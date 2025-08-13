package tarot.infrastructure.services

import shared.infrastructure.services.telegram.TelegramApiServiceLayer
import tarot.application.configurations.AppConfig
import tarot.infrastructure.services.authorize.*
import tarot.infrastructure.services.photo.*
import zio.ZLayer

object TarotServiceLayer {
  val tokenLayer: ZLayer[AppConfig, Nothing, String] =
    ZLayer.fromFunction((config: AppConfig) => config.telegram.token)

  val tarotServiceLive: ZLayer[AppConfig, Throwable, TarotService] =
    (
      PhotoServiceLayer.photoServiceLive ++
      AuthServiceLayer.authServiceLive ++
      FileStorageServiceLayer.localFileStorageServiceLive ++
      (tokenLayer >>> TelegramApiServiceLayer.telegramApiServiceLive)
    ) >>> ZLayer.fromFunction(TarotServiceLive.apply)
}
