package tarot.infrastructure.services.photo

import shared.infrastructure.services.{TelegramApiService, TelegramApiServiceLayer}
import tarot.application.configurations.AppConfig
import tarot.infrastructure.services.TarotServiceLayer
import zio.ZLayer


object PhotoServiceLayer {
  val photoServiceLive: ZLayer[AppConfig, Throwable, PhotoService] =
    ((TarotServiceLayer.tokenLayer >>> TelegramApiServiceLayer.telegramApiServiceLive)
      ++ FileStorageServiceLayer.localFileStorageServiceLive) >>>
      ZLayer.fromFunction { (telegram: TelegramApiService, storage: FileStorageService) =>
        new PhotoServiceLive(telegram, storage)
      }
}
