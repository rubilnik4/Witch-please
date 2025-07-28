package tarot.infrastructure.services

import tarot.application.configurations.AppConfig
import tarot.infrastructure.services.authorize.{AuthService, AuthServiceLayer}
import tarot.infrastructure.services.photo.*
import tarot.infrastructure.services.users.UserService
import zio.ZLayer

object TarotServiceLayer {
  val tarotServiceLive: ZLayer[AppConfig, Throwable, TarotService] =
    (
      PhotoServiceLayer.photoServiceLive ++
      AuthServiceLayer.authServiceLive ++
      FileStorageServiceLayer.localFileStorageServiceLive ++
      TelegramFileServiceLayer.telegramFileServiceLive
      ) >>> ZLayer.fromFunction(TarotServiceLive.apply)
}
