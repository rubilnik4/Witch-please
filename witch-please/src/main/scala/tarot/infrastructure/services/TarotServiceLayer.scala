package tarot.infrastructure.services

import tarot.application.configurations.AppConfig
import tarot.infrastructure.services.auth.{AuthService, AuthServiceLayer}
import tarot.infrastructure.services.photo.*
import zio.ZLayer

object TarotServiceLayer {
  private val tarotServiceLayer: ZLayer[
    PhotoService
      & AuthService
      & FileStorageService
      & TelegramFileService,
    Nothing,
    TarotService
  ] =
    ZLayer.fromFunction(TarotServiceLive.apply)

  val tarotServiceLive: ZLayer[AppConfig, Throwable, TarotService] =
    (PhotoServiceLayer.photoServiceLive ++
      AuthServiceLayer.authServiceLive ++
      FileStorageServiceLayer.localFileStorageServiceLive ++
      TelegramFileServiceLayer.telegramFileServiceLive) >>>
      tarotServiceLayer
}
