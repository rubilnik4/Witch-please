package tarot.application.commands.spreads

import shared.infrastructure.services.common.DateTimeService
import shared.models.tarot.spreads.SpreadStatus
import tarot.application.commands.spreads.commands.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.photo.PhotoSource
import tarot.domain.models.spreads.*
import tarot.infrastructure.repositories.cards.CardRepository
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.layers.TarotEnv
import zio.*

import java.time.{Duration, Instant}

final class SpreadCommandHandlerLive(
  spreadRepository: SpreadRepository,
  cardRepository: CardRepository
) extends SpreadCommandHandler {
  
  override def createSpread(command: CreateSpreadCommand) : ZIO[TarotEnv, TarotError, SpreadId] =
    for {
      _ <- ZIO.logInfo(s"Executing create spread ${command.title} command")

      projectQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.projectQueryHandler)
      projectId <- projectQueryHandler.getDefaultProject(command.userId)

      photoFile <- getPhotoFile(command.photo)
      spread <- Spread.toDomain(command, projectId, photoFile)
      spreadId <- spreadRepository.createSpread(spread)
    } yield spreadId

  override def updateSpread(command: UpdateSpreadCommand): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Executing update spread command for ${command.spreadId}")

      previousSpread <- getSpread(command.spreadId)
      _ <- validateModifyStatus(previousSpread)

      photoFile <- getPhotoFile(command.photo)
      spread <- SpreadUpdate.toDomain(command, photoFile)
      _ <- spreadRepository.updateSpread(command.spreadId, spread)

      photoCommandHandler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.photoCommandHandler)
      _ <- photoCommandHandler.deletePhoto(previousSpread.photo.id, previousSpread.photo.fileId)
    } yield ()

  override def scheduleSpread(command: ScheduleSpreadCommand) : ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Executing schedule spread command for spread ${command.spreadId}")

      spread <- getSpread(command.spreadId)
      _ <- validatePublishing(spread)
      _ <- schedulePublish(spread, command.scheduledAt, command.cardOfDayDelayHours)
    } yield ()

  override def publishPreviewSpread(spreadId: SpreadId): ZIO[TarotEnv, TarotError, Unit] =
    for {  
      _ <- ZIO.logInfo(s"Executing publish preview command for spread $spreadId")
      
      spreadStatusUpdate = SpreadStatusUpdate.PreviewPublished(spreadId)
      _ <- spreadRepository.updateSpreadStatus(spreadStatusUpdate)
    } yield ()

  override def publishSpread(spreadId: SpreadId, publishAt: Instant): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Executing publish command for spread $spreadId")

      spreadStatusUpdate = SpreadStatusUpdate.Published(spreadId, publishAt)
      _ <- spreadRepository.updateSpreadStatus(spreadStatusUpdate)
    } yield ()

  override def deleteSpread(spreadId: SpreadId): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Executing delete command for spread $spreadId")
      
      spread <- getSpread(spreadId)
      _ <- validateModifyStatus(spread)
      _ <- spreadRepository.deleteSpread(spreadId)
    } yield ()

  private def getPhotoFile(photoFile: PhotoSource) =
    for {
      photoService <- ZIO.serviceWith[TarotEnv](_.services.photoService)
      photoFile <- photoService.fetchAndStore(photoFile.sourceId)
    } yield photoFile

  private def validatePublishing(spread: Spread) =
    for {
      _ <- ZIO.logInfo(s"Checking spread before publish for ${spread.id}")

      _ <- ZIO.unless(List(SpreadStatus.Draft, SpreadStatus.Scheduled).contains(spread.spreadStatus)) {
        ZIO.logError(s"Spread $spread.id is not in Draft status") *>
          ZIO.fail(TarotError.Conflict(s"Spread $spread.id is not in Draft status"))
      }   

      cardCount <- cardRepository.getCardsCount(spread.id)
      _ <- ZIO.when(cardCount < spread.cardsCount) {
        ZIO.logError(s"Spread ${spread.id} has only $cardCount out of ${spread.cardsCount} cards") *>
          ZIO.fail(TarotError.Conflict(s"Spread ${spread.id} has only $cardCount out of ${spread.cardsCount} cards"))
      }
    } yield ()

  private def schedulePublish(spread: Spread, scheduledAt: Instant, cardOfDayDelayHours: Duration) =
    for {      
      config <- ZIO.serviceWith[TarotEnv](_.config.publish)

      now <- DateTimeService.getDateTimeNow
      hardPastTime = now.minus(config.hardPastTime)
      maxTime = now.plus(config.maxFutureTime)
      _ <- ZIO.when(scheduledAt.isBefore(hardPastTime) || scheduledAt.isAfter(maxTime)) {
        ZIO.logError(s"scheduledAt must be after $hardPastTime and before $maxTime")
          *> ZIO.fail(ValidationError(s"scheduledAt must be after $hardPastTime and before $maxTime"))
      }
      _ <- ZIO.when(scheduledAt.isBefore(spread.createdAt)) {
        ZIO.logError(s"scheduledAt must be after creation time ${spread.createdAt}")
          *> ZIO.fail(ValidationError(s"scheduledAt must be after creation time ${spread.createdAt}"))
      }
      
      cardOfDayAt = scheduledAt.plus(cardOfDayDelayHours)
      _ <- ZIO.when(cardOfDayDelayHours > config.maxCardOfDayDelay) {
        ZIO.logError(s"Card of day delay shouldn't be more than ${config.maxCardOfDayDelay} hours") *>
          ZIO.fail(TarotError.ValidationError(s"Card of day delay shouldn't be more than ${config.maxCardOfDayDelay} hours"))
      }
      
      _ <- ZIO.logInfo(s"Schedule spread ${spread.id} to publishing")
      spreadStatusUpdate = SpreadStatusUpdate.Scheduled(spread.id, scheduledAt, cardOfDayAt, spread.scheduledAt)
      _ <- spreadRepository.updateSpreadStatus(spreadStatusUpdate)
    } yield ()

  private def validateModifyStatus(spread: Spread) =
    for {
      _ <- ZIO.when(spread.spreadStatus == SpreadStatus.PreviewPublished) {
        ZIO.logError(s"Spread ${spread.id} already preview published, couldn't be modify") *>
          ZIO.fail(TarotError.Conflict(s"Spread ${spread.id} already preview published, couldn't be modify"))
      }
      _ <- ZIO.when(spread.spreadStatus == SpreadStatus.Published) {
        ZIO.logError(s"Spread ${spread.id} already published, couldn't be modify") *>
          ZIO.fail(TarotError.Conflict(s"Spread ${spread.id} already published, couldn't be modify"))
      }
    } yield ()

  private def getSpread(spreadId: SpreadId) =
    spreadRepository.getSpread(spreadId)
      .flatMap(ZIO.fromOption(_).orElseFail(TarotError.NotFound(s"Spread $spreadId not found")))
}
