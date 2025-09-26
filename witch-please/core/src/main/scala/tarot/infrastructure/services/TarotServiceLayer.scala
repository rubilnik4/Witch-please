package tarot.infrastructure.services

import shared.application.configurations.TelegramConfig
import shared.infrastructure.services.files.*
import shared.infrastructure.services.telegram.*
import tarot.application.configurations.*
import tarot.infrastructure.services.authorize.*
import tarot.infrastructure.services.photo.*
import zio.{ZIO, ZLayer}

object TarotServiceLayer {
  private val storedLayer: ZLayer[TarotConfig, Throwable, String] =
    ZLayer.fromZIO {
      for {
        config <- ZIO.service[TarotConfig]
        localStorage <- ZIO.fromOption(config.localStorage)
          .orElseFail(new RuntimeException("Local storage config is missing"))
      } yield localStorage.path
    }
    
  private val tokenLayer: ZLayer[TarotConfig, Nothing, TelegramConfig] =
    ZLayer.fromFunction((config: TarotConfig) => config.telegram)
    
  private val telegramLayer: ZLayer[TarotConfig, Throwable, TelegramChannelService] =
    tokenLayer >>> TelegramChannelServiceLayer.telegramChannelServiceLive

  private val storageLayer: ZLayer[TarotConfig, Throwable, FileStorageService] =
    storedLayer >>> FileStorageServiceLayer.localFileStorageServiceLive

  val tarotServiceLive: ZLayer[TarotConfig, Throwable, TarotService] =
    (
      ((telegramLayer ++ storageLayer) >>> PhotoServiceLayer.photoServiceLive) ++
      AuthServiceLayer.authServiceLive ++ storageLayer ++ telegramLayer
    ) >>> ZLayer.fromFunction(TarotServiceLive.apply)
}
