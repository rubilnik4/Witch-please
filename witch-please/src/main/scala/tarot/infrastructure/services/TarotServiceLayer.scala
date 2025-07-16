package tarot.infrastructure.services

import tarot.application.configurations.AppConfig
import tarot.infrastructure.services.auth.{AuthService, AuthServiceLayer}
import tarot.infrastructure.services.photo.*
import tarot.infrastructure.services.users.{UserService, UserServiceLayer}
import zio.ZLayer

object TarotServiceLayer {
  private val tarotServiceLayer: ZLayer[
    PhotoService
      & AuthService & UserService
      & FileStorageService
      & TelegramFileService,
    Nothing,
    TarotService
  ] =
    ZLayer.fromFunction(TarotServiceLive.apply)

  val tarotServiceLive: ZLayer[AppConfig, Throwable, TarotService] =
    (PhotoServiceLayer.photoServiceLive ++
      AuthServiceLayer.authServiceLive ++ UserServiceLayer.userServiceLive ++
      FileStorageServiceLayer.localFileStorageServiceLive ++
      TelegramFileServiceLayer.telegramFileServiceLive) >>>
      tarotServiceLayer
}
