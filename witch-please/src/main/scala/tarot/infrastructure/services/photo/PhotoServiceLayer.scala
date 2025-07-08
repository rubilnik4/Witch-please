package tarot.infrastructure.services.photo

import tarot.application.configurations.AppConfig
import tarot.infrastructure.services.photo.FileStorageServiceLayer.localFileStorageServiceLive
import tarot.infrastructure.services.photo.TelegramFileServiceLayer.telegramFileServiceLive
import zio.ZLayer


object PhotoServiceLayer {
  val photoServiceLive: ZLayer[AppConfig, Throwable, PhotoService] =
    (telegramFileServiceLive ++ localFileStorageServiceLive) >>> ZLayer.fromFunction(
      (telegram: TelegramFileService, storage: FileStorageService) =>
        new PhotoServiceLive(telegram, storage))
}
