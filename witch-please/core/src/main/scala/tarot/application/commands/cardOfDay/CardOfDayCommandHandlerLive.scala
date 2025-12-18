package tarot.application.commands.cardOfDay

import shared.models.files.FileStorage
import tarot.application.commands.cardOfDay.commands.CreateCardOfDayCommand
import tarot.application.commands.cards.commands.{CreateCardCommand, UpdateCardCommand}
import tarot.application.commands.spreads.SpreadValidateHandler
import tarot.domain.models.TarotError
import tarot.domain.models.cardOfDay.{CardOfDay, CardOfDayId, CardOfDayStatusUpdate}
import tarot.domain.models.cards.*
import tarot.domain.models.photo.PhotoSource
import tarot.domain.models.spreads.SpreadId
import tarot.infrastructure.repositories.cardOfDay.CardOfDayRepository
import tarot.infrastructure.repositories.cards.CardRepository
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

      photoFile <- getPhotoSource(command.photo)
      cardOfDay <- CardOfDay.toDomain(command, photoFile)
      cardOfDayId <- cardOfDayRepository.createCardOfDay(cardOfDay)
    } yield cardOfDayId

  override def publishCardOfDay(cardOfDayId: CardOfDayId, publishAt: Instant): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Executing publish command for card of day $cardOfDayId")

      cardOfDayStatusUpdate = CardOfDayStatusUpdate.Published(cardOfDayId, publishAt)
      _ <- cardOfDayRepository.updateCardOfDayStatus(cardOfDayStatusUpdate)
    } yield ()
    
  private def getPhotoSource(photoFile: PhotoSource): ZIO[TarotEnv, TarotError, FileStorage] =
    for {
      photoService <- ZIO.serviceWith[TarotEnv](_.services.photoService)
      photoFile <- photoService.fetchAndStore(photoFile.sourceId)
    } yield photoFile
}
