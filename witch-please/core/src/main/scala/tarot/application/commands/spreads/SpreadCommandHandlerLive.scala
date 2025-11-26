package tarot.application.commands.spreads

import shared.infrastructure.services.common.DateTimeService
import shared.models.tarot.spreads.SpreadStatus
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.authorize.UserId
import tarot.domain.models.photo.ExternalPhoto
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
  
  override def createSpread(externalSpread: ExternalSpread, userId: UserId) : ZIO[TarotEnv, TarotError, SpreadId] =
    for {
      _ <- ZIO.logInfo(s"Executing create spread command for $externalSpread")
      
      projectQueryHandler <- ZIO.serviceWith[TarotEnv](_.tarotQueryHandler.projectQueryHandler)
      projectId <- projectQueryHandler.getDefaultProject(userId)
      
      photoSource <- getPhotoSource(externalSpread)
      spread <- Spread.toDomain(externalSpread, projectId, photoSource)
      spreadId <- spreadRepository.createSpread(spread)
    } yield spreadId

  override def scheduleSpread(spreadId: SpreadId, scheduledAt: Instant, cardOfDayDelayHours: Duration) : ZIO[TarotEnv, TarotError, Unit] =
    for {
      spread <- spreadRepository.getSpread(spreadId)
        .flatMap(ZIO.fromOption(_).orElseFail(TarotError.NotFound(s"Spread $spreadId not found")))

      _ <- checkingPublish(spread)
      _ <- schedulePublish(spread, scheduledAt, cardOfDayDelayHours)
    } yield ()

  override def publishPreviewSpread(spread: Spread): ZIO[TarotEnv, TarotError, Unit] =
    for {  
      _ <- ZIO.logInfo(s"Publish preview spread ${spread.id}")
      
      spreadStatusUpdate = SpreadStatusUpdate.PreviewPublished(spread.id)
      _ <- spreadRepository.updateSpreadStatus(spreadStatusUpdate)
    } yield ()

  override def publishSpread(spread: Spread, publishAt: Instant): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Publish spread ${spread.id}")

      spreadStatusUpdate = SpreadStatusUpdate.Published(spread.id, publishAt)
      _ <- spreadRepository.updateSpreadStatus(spreadStatusUpdate)
    } yield ()
    
  override def deleteSpread(spreadId: SpreadId): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Executing delete spread command for $spreadId")
      
      spread <- spreadRepository.getSpread(spreadId)
        .flatMap(ZIO.fromOption(_).orElseFail(TarotError.NotFound(s"Spread $spreadId not found")))

      _ <- ZIO.when(spread.spreadStatus == SpreadStatus.PreviewPublished) {
        ZIO.logError(s"Spread ${spread.id} already preview published, couldn't be deleted") *>
          ZIO.fail(TarotError.Conflict(s"Spread ${spread.id} already preview published, couldn't be deleted"))
      }
      _ <- ZIO.when(spread.spreadStatus == SpreadStatus.Published) {
        ZIO.logError(s"Spread ${spread.id} already published, couldn't be deleted") *>
          ZIO.fail(TarotError.Conflict(s"Spread ${spread.id} already published, couldn't be deleted"))
      }

      _ <- spreadRepository.deleteSpread(spreadId)

      _ <- ZIO.logInfo(s"Successfully spread deleted: $spreadId")
    } yield ()

  private def getPhotoSource(externalSpread: ExternalSpread) =
    for {
      photoService <- ZIO.serviceWith[TarotEnv](_.tarotService.photoService)

      storedPhoto <- externalSpread.coverPhoto match {
        case ExternalPhoto.Telegram(fileId) => photoService.fetchAndStore(fileId)
      }
    } yield storedPhoto

  private def checkingPublish(spread: Spread) =
    for {
      _ <- ZIO.logInfo(s"Checking spread before publish for ${spread.id}")

      _ <- ZIO.unless(List(SpreadStatus.Draft, SpreadStatus.Scheduled).contains(spread.spreadStatus)) {
        ZIO.logError(s"Spread $spread.id is not in Draft status") *>
          ZIO.fail(TarotError.Conflict(s"Spread $spread.id is not in Draft status"))
      }   

      cardCount <- cardRepository.getCardsCount(spread.id)
      _ <- ZIO.when(cardCount < spread.cardCount) {
        ZIO.logError(s"Spread ${spread.id} has only $cardCount out of ${spread.cardCount} cards") *>
          ZIO.fail(TarotError.Conflict(s"Spread ${spread.id} has only $cardCount out of ${spread.cardCount} cards"))
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
      _ <- ZIO.when(cardOfDayDelayHours > config.maxCardOfDayDelayHours) {
        ZIO.logError(s"Card of day delay shouldn't be more than ${config.maxCardOfDayDelayHours} hours") *>
          ZIO.fail(TarotError.ValidationError(s"Card of day delay shouldn't be more than ${config.maxCardOfDayDelayHours} hours"))
      }
      
      _ <- ZIO.logInfo(s"Schedule spread ${spread.id} to publishing")
      spreadStatusUpdate = SpreadStatusUpdate.Scheduled(spread.id, scheduledAt, cardOfDayAt, spread.scheduledAt)
      _ <- spreadRepository.updateSpreadStatus(spreadStatusUpdate)
    } yield ()
}
