package tarot.application.commands.cards

import shared.models.photo.PhotoSource
import tarot.application.commands.cards.commands.{CreateCardCommand, UpdateCardCommand}
import tarot.application.commands.spreads.SpreadValidateHandler
import tarot.domain.models.TarotError
import tarot.domain.models.cards.*
import tarot.domain.models.spreads.SpreadId
import tarot.infrastructure.repositories.cards.CardRepository
import tarot.layers.TarotEnv
import zio.ZIO

final class CardCommandHandlerLive(
  cardRepository: CardRepository
) extends CardCommandHandler {

  override def createCard(command: CreateCardCommand): ZIO[TarotEnv, TarotError, CardId] =
    for {
      _ <- ZIO.logInfo(s"Executing create card ${command.title} command for spread ${command.spreadId}")

      spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
      spread <- spreadQueryHandler.getSpread(command.spreadId)
      _ <- SpreadValidateHandler.validateModifyStatus(spread)

      _ <- ZIO.when(command.position < 0) {
        ZIO.logError(s"Card position must be > 0, got ${command.position}") *>
          ZIO.fail(TarotError.ValidationError(s"Card position must be > 0, got ${command.position}"))
      }

      existCardPosition <- cardRepository.existCardPosition(command.spreadId, command.position)
      _ <- ZIO.when(existCardPosition) {
        ZIO.logError(s"Card position ${command.position} already exists in spread ${command.spreadId}") *>
          ZIO.fail(TarotError.Conflict(s"Card position ${command.position} already exists in spread ${command.spreadId}"))
      }

      photoService <- ZIO.serviceWith[TarotEnv](_.services.photoService)
      photoFile <- photoService.fetchAndStore(command.photo)
      card <- Card.toDomain(command, photoFile.fileStored)
      cardId <- cardRepository.createCard(card)
    } yield cardId

  override def updateCard(command: UpdateCardCommand): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Executing update card ${command.cardId} command")

      cardQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardQueryHandler)
      previousCard <- cardQueryHandler.getCard(command.cardId)

      spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
      spread <- spreadQueryHandler.getSpread(previousCard.spreadId)
      _ <- SpreadValidateHandler.validateModifyStatus(spread)

      photoService <- ZIO.serviceWith[TarotEnv](_.services.photoService)
      photoFile <- photoService.fetchAndStore(command.photo)
      card = CardUpdate.toDomain(command, photoFile.fileStored)
      _ <- cardRepository.updateCard(command.cardId, card)

      photoCommandHandler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.photoCommandHandler)
      _ <- photoCommandHandler.deletePhoto(previousCard.photo.id, previousCard.photo.fileId)
    } yield ()

  override def deleteCard(cardId: CardId): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Executing delete command for card $cardId")

      cardQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardQueryHandler)
      spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
      cardOfDayQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardOfDayQueryHandler)
      card <- cardQueryHandler.getCard(cardId)    
      spread <- spreadQueryHandler.getSpread(card.spreadId)
      _ <- SpreadValidateHandler.validateModifyStatus(spread)

      cardOfDayMaybe <- cardOfDayQueryHandler.getCardOfDayByCardOption(cardId)
      _ <- deleteCard(card)

      photoCommandHandler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.photoCommandHandler)
      photos = cardOfDayMaybe.map(_.photo).toList
      _ <- ZIO.foreachParDiscard(photos) { photo =>
        photoCommandHandler.deletePhoto(photo.id, photo.fileId)
      }
    } yield ()
    
  override def deleteCard(card: Card): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Executing partial delete command for card ${card.id}")

      _ <- cardRepository.deleteCard(card.id)
      
      photoCommandHandler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.photoCommandHandler)
      _ <- photoCommandHandler.deletePhoto(card.photo.id, card.photo.fileId)
    } yield ()

  override def cloneCards(spreadId: SpreadId, cloneSpreadId: SpreadId): ZIO[TarotEnv, TarotError, List[CardId]] =
    for {
      _ <- ZIO.logInfo(s"Executing clone cards command by spread $spreadId")

      cardQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardQueryHandler)
      photoService <- ZIO.serviceWith[TarotEnv](_.services.photoService)
      
      cards <- cardQueryHandler.getCards(spreadId)
      photoSources = cards.map(card => PhotoSource(card.photo.sourceId, card.photo.sourceType, Some(card.id.id.toString)))
      photoFiles <- photoService.fetchAndStore(photoSources)
      clonedCards <- ZIO.foreach(cards) { card =>       
        for {
          photoFile <- ZIO.fromOption(photoFiles.find(photoFile => photoFile.photoSource.parentId.contains(card.id.id.toString)))
            .orElseFail(TarotError.NotFound(s"Photo file not found for ${card.id}"))
          cloned <- Card.clone(card, cloneSpreadId, photoFile.fileStored)
        } yield cloned
      }
      cardIds <- cardRepository.createCards(clonedCards)
    } yield cardIds
}
