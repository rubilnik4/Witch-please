package tarot.application.commands.spreads

import tarot.application.commands.spreads.CardCreateCommand
import tarot.domain.models.TarotError
import tarot.domain.models.cards.*
import tarot.domain.models.photo.ExternalPhoto
import tarot.layers.TarotEnv
import zio.ZIO


final class CardCreateCommandHandlerLive extends CardCreateCommandHandler {
  def handle(command: CardCreateCommand): ZIO[TarotEnv, TarotError, CardId] = {
    for {
      _ <- ZIO.logInfo(s"Executing create card command for ${command.externalCard}")

      spreadRepository <- ZIO.serviceWith[TarotEnv](_.tarotRepository.spreadRepository)

      exists <- spreadRepository.existsSpread(command.externalCard.spreadId)
      _ <- ZIO.unless(exists) {
        ZIO.logError(s"Spread ${command.externalCard.spreadId} not found for card create") *>
          ZIO.fail(TarotError.NotFound(s"Spread ${command.externalCard.spreadId} not found"))
      }

      card <- fetchAndStorePhoto(command.externalCard)
      cardId <- spreadRepository.createCard(card)

      _ <- ZIO.logInfo(s"Successfully card created: $cardId")
    } yield cardId
  }

  private def fetchAndStorePhoto(externalCard: ExternalCard): ZIO[TarotEnv, TarotError, Card] = {
    for {
      photoService <- ZIO.serviceWith[TarotEnv](_.tarotService.photoService)

      storedPhoto <- externalCard.coverPhotoId match {
        case ExternalPhoto.Telegram(fileId) => photoService.fetchAndStore(fileId)
      }
      card <- Card.toDomain(externalCard, storedPhoto)
    } yield card
  }
}
