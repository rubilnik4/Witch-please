package tarot.application.commands.spreads

import shared.infrastructure.services.common.DateTimeService
import shared.models.tarot.spreads.SpreadStatus
import tarot.application.commands.spreads.commands.*
import tarot.domain.models.{TarotError, TarotErrorMapper}
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.cards.Card
import tarot.domain.models.cardsOfDay.CardOfDay
import tarot.domain.models.photo.PhotoSource
import tarot.domain.models.spreads.*
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.layers.TarotEnv
import zio.*

import java.time.{Duration, Instant}

final class SpreadCommandHandlerLive(
  spreadRepository: SpreadRepository
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

      spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
      cardQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardQueryHandler)
      previousSpread <- spreadQueryHandler.getSpread(command.spreadId)
      _ <- SpreadValidateHandler.validateModifyStatus(previousSpread)
      cards <- cardQueryHandler.getCards(command.spreadId)

      photoFile <- getPhotoFile(command.photo)
      spread = SpreadUpdate.toDomain(command, photoFile)
      _ <- spreadRepository.updateSpread(command.spreadId, spread)

      photoCommandHandler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.photoCommandHandler)
      _ <- photoCommandHandler.deletePhoto(previousSpread.photo.id, previousSpread.photo.fileId)
    } yield ()

  override def scheduleSpread(command: ScheduleSpreadCommand): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Executing schedule spread command for spread ${command.spreadId}")

      userChannelQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.userChannelQueryHandler)
      spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
      cardOfDayQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardOfDayQueryHandler)
      cardQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardQueryHandler)
      
      _ <- userChannelQueryHandler.validateUserChannels(command.userId)
      
      spread <- spreadQueryHandler.getSpread(command.spreadId)      
      cards <- cardQueryHandler.getCards(spread.id)
      _ <- validatePublishing(spread, cards)

      cardOfDay <- cardOfDayQueryHandler.getCardOfDayBySpread(command.spreadId)
      _ <- scheduleSpread(spread, command.scheduledAt, cardOfDay, command.cardOfDayDelayHours)
      _ <- deleteCardsOnPublish(spread, cards)
    } yield ()

  override def publishSpread(spread: Spread, publishAt: Instant): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Executing publish command for spread ${spread.id}")

      userChannelQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.userChannelQueryHandler)
      userChannel <- userChannelQueryHandler.getUserChannelByProject(spread.projectId)

      telegramApiService <- ZIO.serviceWith[TarotEnv](_.services.telegramApiService)
      _ <- telegramApiService.sendPhoto(userChannel.channelId, spread.photo.sourceId).mapError(TarotErrorMapper.toTarotError)
       
      spreadStatusUpdate = SpreadStatusUpdate.Published(spread.id, publishAt)
      _ <- spreadRepository.updateSpreadStatus(spreadStatusUpdate)
    } yield ()

  override def deleteSpread(spreadId: SpreadId): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Executing delete command for spread $spreadId")

      spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
      cardQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardQueryHandler)
      cardOfDayQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardOfDayQueryHandler)
      spread <- spreadQueryHandler.getSpread(spreadId)
      _ <- SpreadValidateHandler.validateModifyStatus(spread)
      
      cards <- cardQueryHandler.getCards(spreadId)
      cardOfDayMaybe <- cardOfDayQueryHandler.getCardOfDayBySpreadOption(spreadId)
      _ <- spreadRepository.deleteSpread(spreadId)

      photoCommandHandler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.photoCommandHandler)
      photos = spread.photo :: cards.map(_.photo) ++ cardOfDayMaybe.map(_.photo).toList
      _ <- ZIO.foreachParDiscard(photos) { photo =>
        photoCommandHandler.deletePhoto(photo.id, photo.fileId)
      }
    } yield ()

  private def getPhotoFile(photoFile: PhotoSource) =
    for {
      photoService <- ZIO.serviceWith[TarotEnv](_.services.photoService)
      photoFile <- photoService.fetchAndStore(photoFile.sourceId)
    } yield photoFile

  private def validatePublishing(spread: Spread, cards: List[Card]) =
    for {
      _ <- ZIO.logInfo(s"Checking spread before publish for ${spread.id}")

      _ <- ZIO.unless(List(SpreadStatus.Draft, SpreadStatus.Scheduled).contains(spread.status)) {
        ZIO.logError(s"Spread $spread.id is not in Draft or Scheduled status") *>
          ZIO.fail(TarotError.Conflict(s"Spread $spread.id is not in Draft or Scheduled status"))
      }

      _ <- ZIO.when(cards.size < spread.cardsCount) {
        ZIO.logError(s"Spread ${spread.id} has only ${cards.size} out of ${spread.cardsCount} cards") *>
          ZIO.fail(TarotError.Conflict(s"Spread ${spread.id} has only ${cards.size} out of ${spread.cardsCount} cards"))
      }
    } yield ()

  private def scheduleSpread(spread: Spread, scheduledAt: Instant, cardOfDay: CardOfDay, cardOfDayDelay: Duration) =
    for {
      _ <- ZIO.logInfo(s"Schedule spread ${spread.id} to publishing")

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

      cardOfDayAt <- getCardOfDayAt(scheduledAt, cardOfDayDelay)
      spreadStatusUpdate = SpreadStatusUpdate.Scheduled(spread.id, scheduledAt, cardOfDay.id, cardOfDayAt)
      _ <- spreadRepository.updateSpreadStatus(spreadStatusUpdate)
    } yield ()

  private def getCardOfDayAt(scheduledAt: Instant, cardOfDayDelayHours: Duration) =
    for {
      config <- ZIO.serviceWith[TarotEnv](_.config.publish)

      _ <- ZIO.when(cardOfDayDelayHours > config.maxCardOfDayDelay) {
        ZIO.logError(s"Card of day delay shouldn't be more than ${config.maxCardOfDayDelay} hours") *>
          ZIO.fail(TarotError.ValidationError(s"Card of day delay shouldn't be more than ${config.maxCardOfDayDelay} hours"))
      }
      cardOfDayAt = scheduledAt.plus(cardOfDayDelayHours)
    } yield cardOfDayAt

  private def deleteCardsOnPublish(spread: Spread, cards: List[Card]) =
    for {
      _ <- ZIO.logInfo(s"Deleting cards on publishing by ${spread.id}")

      cardsToDelete =
        if spread.cardsCount < cards.length then
          cards.sortBy(_.position).takeRight(cards.length - spread.cardsCount)
        else
          Nil

      cardCommandHandler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.cardCommandHandler)
      _ <- ZIO.foreachParDiscard(cardsToDelete) { card =>
        cardCommandHandler.deleteCard(card)
      }
    } yield ()
}
