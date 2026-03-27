package tarot.infrastructure.services.photo

import shared.infrastructure.services.storage.FileStorageService
import shared.infrastructure.services.telegram.TelegramApiService
import tarot.infrastructure.repositories.photo.PhotoRepository
import zio.ZLayer


object PhotoServiceLayer {
  val photoServiceLive: ZLayer[TelegramApiService & FileStorageService & PhotoRepository, Throwable, PhotoService] =
      ZLayer.fromFunction { (telegram: TelegramApiService, storage: FileStorageService, photoRepository: PhotoRepository) =>
        new PhotoServiceLive(telegram, storage, photoRepository)
      }
}
