package tarot.infrastructure.services.photo

import tarot.domain.models.TarotError
import tarot.domain.models.photo.{PhotoFile, PhotoSource}
import tarot.infrastructure.services.photo.{FileStorageService, TelegramFileService}
import zio.ZIO

final class PhotoServiceLive(telegram: TelegramFileService, storage: FileStorageService)
  extends PhotoService:

  def fetchAndStore(fileId: String): ZIO[Any, TarotError, PhotoSource] =
    for {
      _ <- ZIO.logInfo(s"Downloading photo: $fileId")
      photoFile <- telegram.downloadPhoto(fileId)
        .tapError(err => ZIO.logError(s"Failed to download photo from Telegram. FileId: $fileId. Error: $err"))

      _ <- ZIO.logInfo(s"Storing photo: ${photoFile.fileName}")
      photoSource <- storage.storePhoto(photoFile)
        .tapError(err => ZIO.logError(s"Failed to store photo: ${photoFile.fileName}. Error: $err"))
    } yield photoSource