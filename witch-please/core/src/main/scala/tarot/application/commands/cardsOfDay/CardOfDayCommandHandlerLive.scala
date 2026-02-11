package tarot.application.commands.cardsOfDay

import tarot.application.commands.cardsOfDay.commands.{CreateCardOfDayCommand, UpdateCardOfDayCommand}
import tarot.application.commands.spreads.SpreadValidateHandler
import tarot.domain.models.TarotError
import tarot.domain.models.cardsOfDay.{CardOfDay, CardOfDayId, CardOfDayStatusUpdate, CardOfDayUpdate}
import tarot.domain.models.photo.Photo
import tarot.domain.models.spreads.SpreadId
import tarot.infrastructure.repositories.cardsOfDay.CardOfDayRepository
import tarot.layers.TarotEnv
import zio.ZIO

import java.time.Instant

final class CardOfDayCommandHandlerLive(
  cardOfDayRepository: CardOfDayRepository
) extends CardOfDayCommandHandler {

  override def createCardOfDay(command: CreateCardOfDayCommand): ZIO[TarotEnv, TarotError, CardOfDayId] =
    for {
      _ <- ZIO.logInfo(s"Executing create card of day ${command.cardId} command for spread ${command.spreadId}")

      spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
      spread <- spreadQueryHandler.getSpread(command.spreadId)
      _ <- SpreadValidateHandler.validateModifyStatus(spread)

      existCardOfDay <- cardOfDayRepository.existCardOfDay(command.spreadId)
      _ <- ZIO.when(existCardOfDay) {
        ZIO.logError(s"Card of day already exists in spread ${command.spreadId}") *>
          ZIO.fail(TarotError.Conflict(s"Card of day already exists in spread ${command.spreadId}"))
      }

      photoService <- ZIO.serviceWith[TarotEnv](_.services.photoService)
      photoFile <- photoService.fetchAndStore(command.photo)
      cardOfDay <- CardOfDay.toDomain(command, photoFile.fileStored)
      cardOfDayId <- cardOfDayRepository.createCardOfDay(cardOfDay)
    } yield cardOfDayId
  
  override def updateCardOfDay(command: UpdateCardOfDayCommand): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Executing update card of day ${command.cardOfDayId} command")

      cardOfDayQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardOfDayQueryHandler)
      previousCardOfDay <- cardOfDayQueryHandler.getCardOfDay(command.cardOfDayId)

      spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
      spread <- spreadQueryHandler.getSpread(previousCardOfDay.spreadId)
      _ <- SpreadValidateHandler.validateModifyStatus(spread)

      photoService <- ZIO.serviceWith[TarotEnv](_.services.photoService)
      photoFile <- photoService.fetchAndStore(command.photo)
      cardOfDay = CardOfDayUpdate.toDomain(command, photoFile.fileStored)
      _ <- cardOfDayRepository.updateCardOfDay(command.cardOfDayId, cardOfDay)

      photoCommandHandler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.photoCommandHandler)
      _ <- photoCommandHandler.deletePhoto(previousCardOfDay.photo.id, previousCardOfDay.photo.fileId)
    } yield ()

  override def deleteCardOfDay(cardOfDayId: CardOfDayId): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Executing delete command for card of day $cardOfDayId")

      cardOfDayQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardOfDayQueryHandler)
      cardOfDay <- cardOfDayQueryHandler.getCardOfDay(cardOfDayId)

      spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
      spread <- spreadQueryHandler.getSpread(cardOfDay.spreadId)
      _ <- SpreadValidateHandler.validateModifyStatus(spread)

      _ <- cardOfDayRepository.deleteCardOfDay(cardOfDay.id)

      photoCommandHandler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.photoCommandHandler)
      _ <- photoCommandHandler.deletePhoto(cardOfDay.photo.id, cardOfDay.photo.fileId)
    } yield ()
      
  override def publishCardOfDay(cardOfDayId: CardOfDayId, publishAt: Instant): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Executing publish command for card of day $cardOfDayId")

      cardOfDayStatusUpdate = CardOfDayStatusUpdate.Published(cardOfDayId, publishAt)
      _ <- cardOfDayRepository.updateCardOfDayStatus(cardOfDayStatusUpdate)
    } yield ()

  override def cloneCardOfDay(spreadId: SpreadId, cloneSpreadId: SpreadId): ZIO[TarotEnv, TarotError, CardOfDayId] =
    for {
      _ <- ZIO.logInfo(s"Executing clone card of day command by spread $spreadId")

      cardOfDayQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardOfDayQueryHandler)
      photoService <- ZIO.serviceWith[TarotEnv](_.services.photoService)

      cardOfDay <- cardOfDayQueryHandler.getCardOfDayBySpread(spreadId)
      photoFile <- photoService.fetchAndStore(Photo.toPhotoSource(cardOfDay.photo))
      cloneCardOfDay <- CardOfDay.clone(cardOfDay, cloneSpreadId, photoFile.fileStored)
      cardOfDayId <- cardOfDayRepository.createCardOfDay(cloneCardOfDay)
    } yield cardOfDayId
}
