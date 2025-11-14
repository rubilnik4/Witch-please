package tarot.application.commands.spreads

import shared.infrastructure.services.common.DateTimeService
import shared.models.tarot.spreads.SpreadStatus
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.photo.ExternalPhoto
import tarot.domain.models.spreads.*
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.layers.TarotEnv
import zio.ZIO

import java.time.Instant

final class SpreadCommandHandlerLive(spreadRepository: SpreadRepository) extends SpreadCommandHandler {
  def createSpread(externalSpread: ExternalSpread) : ZIO[TarotEnv, TarotError, SpreadId] =
    for {
      _ <- ZIO.logInfo(s"Executing create spread command for $externalSpread")

      spread <- fetchAndStorePhoto(externalSpread)      
      spreadId <- spreadRepository.createSpread(spread)

      _ <- ZIO.logInfo(s"Successfully spread created: $spreadId")
    } yield spreadId  

  def scheduleSpread(spreadId: SpreadId, scheduledAt: Instant) : ZIO[TarotEnv, TarotError, Unit] =
    for {      
      spread <- spreadRepository.getSpread(spreadId)
        .flatMap(ZIO.fromOption(_).orElseFail(TarotError.NotFound(s"Spread $spreadId not found")))

      _ <- checkingPublish(spread)
      _ <- schedulePublish(spread, scheduledAt)
    } yield ()

  def publishSpread(spread: Spread, publishAt: Instant): ZIO[TarotEnv, TarotError, Unit] =
    for {  
      _ <- ZIO.logInfo(s"Publish spread ${spread.id}")
      spreadStatusUpdate = SpreadStatusUpdate.Published(spread.id, publishAt)
      _ <- spreadRepository.updateSpreadStatus(spreadStatusUpdate)
    } yield ()
  
  def deleteSpread(spreadId: SpreadId): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Executing delete spread command for $spreadId")
      
      spread <- spreadRepository.getSpread(spreadId)
        .flatMap(ZIO.fromOption(_).orElseFail(TarotError.NotFound(s"Spread $spreadId not found")))

      _ <- ZIO.when(spread.publishedAt.isDefined) {
        ZIO.logError(s"Spread ${spread.id} already published, couldn't be deleted") *>
          ZIO.fail(TarotError.Conflict(s"Spread ${spread.id} already published, couldn't be deleted"))
      }

      _ <- spreadRepository.deleteSpread(spreadId)

      _ <- ZIO.logInfo(s"Successfully spread deleted: $spreadId")
    } yield ()

  private def fetchAndStorePhoto(externalSpread: ExternalSpread): ZIO[TarotEnv, TarotError, Spread] =
    for {
      photoService <- ZIO.serviceWith[TarotEnv](_.tarotService.photoService)

      storedPhoto <- externalSpread.coverPhoto match {
        case ExternalPhoto.Telegram(fileId) => photoService.fetchAndStore(fileId)
      }
      spread <- Spread.toDomain(externalSpread, storedPhoto)
    } yield spread
    
  private def checkingPublish(spread: Spread) =
    for {
      _ <- ZIO.logInfo(s"Checking spread before publish for ${spread.id}")
      
      cardCount <- spreadRepository.getCardsCount(spread.id)

      _ <- ZIO.unless(List(SpreadStatus.Draft, SpreadStatus.Scheduled).contains(spread.spreadStatus)) {
        ZIO.logError(s"Spread $spread.id is not in Draft status") *>
          ZIO.fail(TarotError.Conflict(s"Spread $spread.id is not in Draft status"))
      }

      _ <- ZIO.when(cardCount < spread.cardCount) {
        ZIO.logError(s"Spread ${spread.id} has only $cardCount out of ${spread.cardCount} cards") *>
          ZIO.fail(TarotError.Conflict(s"Spread $spread.id has only $cardCount out of ${spread.cardCount} cards"))
      }
    } yield ()

  private def schedulePublish(spread: Spread, scheduledAt: Instant) =
    for {      
      projectConfig <- ZIO.serviceWith[TarotEnv](_.config.project)

      now <- DateTimeService.getDateTimeNow
      hardPastTime = now.minus(projectConfig.hardPastTime)
      maxTime = now.plus(projectConfig.maxFutureTime)
      _ <- ZIO.when(scheduledAt.isBefore(hardPastTime) || scheduledAt.isAfter(maxTime)) {
        ZIO.logError(s"scheduledAt must be after $hardPastTime and before $maxTime")
          *> ZIO.fail(ValidationError(s"scheduledAt must be after $hardPastTime and before $maxTime"))
      }
      _ <- ZIO.when(scheduledAt.isBefore(spread.createdAt)) {
        ZIO.logError(s"scheduledAt must be after creation time ${spread.createdAt}")
          *> ZIO.fail(ValidationError(s"scheduledAt must be after creation time ${spread.createdAt}"))
      }

      _ <- ZIO.logInfo(s"Schedule spread ${spread.id} to publishing")
      spreadStatusUpdate = SpreadStatusUpdate.Scheduled(spread.id, scheduledAt, spread.scheduledAt)
      _ <- spreadRepository.updateSpreadStatus(spreadStatusUpdate)
    } yield ()
}
