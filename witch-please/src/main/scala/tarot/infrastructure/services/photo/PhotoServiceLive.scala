package tarot.infrastructure.services.photo

import tarot.domain.models.TarotError
import tarot.domain.models.photo.PhotoSource
import tarot.infrastructure.services.photo.{FileStorageService, TelegramDownloader}
import zio.ZIO

final class PhotoServiceLive(telegram: TelegramDownloader, storage: FileStorageService) extends PhotoService:

  def fetchAndStore(fileId: String): ZIO[Any, TarotError, PhotoSource] =
    for
      _ <- ZIO.logInfo(s"Downloading photo: $fileId")
      photoFile <- telegram.download(fileId)
        .tapError(err => ZIO.logError(s"Failed to download photo from Telegram. FileId: $fileId. Error: $err"))

      _ <- ZIO.logInfo(s"Storing photo: ${photoFile.fileName}")
      photoSource <- storage.storePhoto(photoFile)
        .tapError(err => ZIO.logError(s"Failed to store photo: ${photoFile.fileName}. Error: $err"))
    yield photoSource
