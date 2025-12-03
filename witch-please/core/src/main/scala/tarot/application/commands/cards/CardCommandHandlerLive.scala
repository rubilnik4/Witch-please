package tarot.application.commands.cards

import shared.models.files.FileStorage
import tarot.application.commands.cards.commands.CreateCardCommand
import tarot.application.commands.spreads.SpreadValidateHandler
import tarot.domain.models.TarotError
import tarot.domain.models.cards.*
import tarot.domain.models.photo.PhotoSource
import tarot.infrastructure.repositories.cards.CardRepository
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.layers.TarotEnv
import zio.ZIO


final class CardCommandHandlerLive(
  cardRepository: CardRepository
) extends CardCommandHandler {

  override def createCard(command: CreateCardCommand): ZIO[TarotEnv, TarotError, CardId] = {
    for {
      _ <- ZIO.logInfo(s"Executing create card ${command.title} command")

      spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
      spread <- spreadQueryHandler.getSpread(command.spreadId)
      _ <- SpreadValidateHandler.validateModifyStatus(spread)

      _ <- ZIO.when(command.position <= 0) {
        ZIO.logError(s"Card position must be > 0, got ${command.position}") *>
          ZIO.fail(TarotError.ValidationError(s"Card position must be > 0, got ${command.position}"))
      }

      existCardPosition <- cardRepository.existCardPosition(command.spreadId, command.position)
      _ <- ZIO.when(existCardPosition) {
        ZIO.logError(s"Card position ${command.position} already exists in spread ${command.spreadId}") *>
          ZIO.fail(TarotError.ValidationError(s"Card position ${command.position} already exists in spread ${command.spreadId}"))
      }

      photoFile <- getPhotoSource(command.photo)
      card <- Card.toDomain(command, photoFile)
      cardId <- cardRepository.createCard(card)
    } yield cardId
  }

  override def deleteCard(card: Card): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Executing delete command for card ${card.id}")

      _ <- cardRepository.deleteCard(card.id)
      
      photoCommandHandler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.photoCommandHandler)
      _ <- photoCommandHandler.deletePhoto(card.photo.id, card.photo.fileId)
    } yield ()
    
  private def getPhotoSource(photoFile: PhotoSource): ZIO[TarotEnv, TarotError, FileStorage] = {
    for {
      photoService <- ZIO.serviceWith[TarotEnv](_.services.photoService)
      photoFile <- photoService.fetchAndStore(photoFile.sourceId)
    } yield photoFile
  }
}
