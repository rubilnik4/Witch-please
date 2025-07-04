package tarot.application.handlers

import tarot.application.commands.{CardCreateCommand, SpreadCreateCommand}
import tarot.domain.models.TarotError
import tarot.domain.models.cards.{Card, CardMapper, ExternalCard}
import tarot.domain.models.contracts.{CardId, SpreadId}
import tarot.domain.models.photo.{ExternalPhoto, Photo}
import tarot.domain.models.spreads.{ExternalSpread, Spread, SpreadMapper}
import tarot.layers.AppEnv
import zio.ZIO

import java.time.Instant

final class CardCreateCommandHandlerLive extends CardCreateCommandHandler {
  def handle(command: CardCreateCommand): ZIO[AppEnv, TarotError, CardId] = {
    for {
      _ <- ZIO.logInfo(s"Executing create card command for ${command.externalCard}")

      tarotRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository)

      exists <- tarotRepository.existsSpread(command.externalCard.spreadId)
      _ <- ZIO.fail(TarotError.NotFound(s"Spread ${command.externalCard.spreadId} not found")).unless(exists)

      card <- fetchAndStorePhoto(command.externalCard)
      cardId <- tarotRepository.createCard(card)

      _ <- ZIO.logInfo(s"Successfully card created: $card")
    } yield cardId
  }

  private def fetchAndStorePhoto(externalCard: ExternalCard): ZIO[AppEnv, TarotError, Card] = {
    for {
      photoService <- ZIO.serviceWith[AppEnv](_.tarotService.photoService)

      storedPhoto <- externalCard.coverPhotoId match {
        case ExternalPhoto.Telegram(fileId) => photoService.fetchAndStore(fileId)
      }
      card = CardMapper.fromExternal(externalCard, storedPhoto)
    } yield card
  }
}
