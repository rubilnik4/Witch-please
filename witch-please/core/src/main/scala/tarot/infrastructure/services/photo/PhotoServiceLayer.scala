package tarot.infrastructure.services.photo

import shared.infrastructure.services.files.{FileStorageService, FileStorageServiceLayer}
import shared.infrastructure.services.telegram.{TelegramApiService, TelegramApiServiceLayer}
import tarot.application.configurations.TarotConfig
import tarot.infrastructure.services.TarotServiceLayer
import zio.ZLayer


object PhotoServiceLayer {
  val photoServiceLive: ZLayer[TelegramApiService & FileStorageService, Throwable, PhotoService] =
      ZLayer.fromFunction { (telegram: TelegramApiService, storage: FileStorageService) =>
        new PhotoServiceLive(telegram, storage)
      }
}
