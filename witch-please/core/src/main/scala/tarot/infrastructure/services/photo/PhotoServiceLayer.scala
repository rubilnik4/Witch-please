package tarot.infrastructure.services.photo

import shared.infrastructure.services.files.{FileStorageService, FileStorageServiceLayer}
import shared.infrastructure.services.telegram.{TelegramChannelService, TelegramChannelServiceLayer}
import tarot.application.configurations.TarotConfig
import tarot.infrastructure.services.TarotServiceLayer
import zio.ZLayer


object PhotoServiceLayer {
  val photoServiceLive: ZLayer[TelegramChannelService & FileStorageService, Throwable, PhotoService] =
      ZLayer.fromFunction { (telegram: TelegramChannelService, storage: FileStorageService) =>
        new PhotoServiceLive(telegram, storage)
      }
}
