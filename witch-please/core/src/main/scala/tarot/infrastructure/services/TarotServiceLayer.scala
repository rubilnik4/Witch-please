package tarot.infrastructure.services

import shared.infrastructure.services.storage.*
import shared.infrastructure.services.telegram.*
import tarot.application.configurations.*
import tarot.infrastructure.repositories.TarotRepositoryLayer
import tarot.infrastructure.repositories.TarotRepositoryLayer.Repositories
import tarot.infrastructure.services.authorize.*
import tarot.infrastructure.services.photo.*
import tarot.infrastructure.services.storage.TarotStorageLayer
import zio.ZLayer

object TarotServiceLayer {
  private val telegramTokenLayer: ZLayer[TarotConfig, Nothing, String] =
    ZLayer.fromFunction((config: TarotConfig) => config.telegram.token)

  private val telegramLayer: ZLayer[TarotConfig, Throwable, TelegramApiService] =
    telegramTokenLayer >>> TelegramApiServiceLayer.live

  val live: ZLayer[TarotConfig & Repositories, Throwable, TarotService] =
    (
      ((telegramLayer ++ TarotStorageLayer.live) >>> PhotoServiceLayer.photoServiceLive) ++
        AuthServiceLayer.live ++ TarotStorageLayer.live ++ ResourceFileServiceLayer.live ++ telegramLayer
    ) >>> ZLayer.fromFunction(TarotServiceLive.apply)
}
