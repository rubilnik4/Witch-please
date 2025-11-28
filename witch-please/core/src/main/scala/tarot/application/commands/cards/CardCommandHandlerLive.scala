package tarot.application.commands.cards

import tarot.domain.models.TarotError
import tarot.domain.models.cards.*
import tarot.domain.models.photo.PhotoFile
import tarot.infrastructure.repositories.cards.CardRepository
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.layers.TarotEnv
import zio.ZIO


final class CardCommandHandlerLive(
  spreadRepository: SpreadRepository, 
  cardRepository: CardRepository
) extends CardCommandHandler {
  override def createCard (externalCard: ExternalCard): ZIO[TarotEnv, TarotError, CardId] = {
    for {
      _ <- ZIO.logInfo(s"Executing create card command for $externalCard")     

      exists <- spreadRepository.existsSpread(externalCard.spreadId)
      _ <- ZIO.unless(exists) {
        ZIO.logError(s"Spread ${externalCard.spreadId} not found for card create") *>
          ZIO.fail(TarotError.NotFound(s"Spread ${externalCard.spreadId} not found"))
      }

      card <- fetchAndStorePhoto(externalCard)
      cardId <- cardRepository.createCard(card)

      _ <- ZIO.logInfo(s"Successfully card created: $cardId")
    } yield cardId
  }

  private def fetchAndStorePhoto(externalCard: ExternalCard): ZIO[TarotEnv, TarotError, Card] = {
    for {
      photoService <- ZIO.serviceWith[TarotEnv](_.tarotService.photoService)

      storedPhoto <- photoService.fetchAndStore(externalCard.coverPhoto.fileId)
      card <- Card.toDomain(externalCard, storedPhoto)
    } yield card
  }
}
