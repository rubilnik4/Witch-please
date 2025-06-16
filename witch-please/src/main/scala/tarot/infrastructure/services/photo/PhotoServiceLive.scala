package tarot.infrastructure.services.photo

import tarot.domain.models.TarotError
import tarot.infrastructure.services.photo.{PhotoStorageService, TelegramPhotoDownloader}
import zio.ZIO

final class PhotoServiceLive(telegram: TelegramPhotoDownloader, storage: PhotoStorageService) extends PhotoService:

  def fetchAndStore(fileId: String): ZIO[Any, TarotError, String] =
    for
      _ <- ZIO.logInfo(s"Downloading photo: $fileId")
      photoFile <- telegram.download(fileId)
        .tapError(err => ZIO.logError(s"Failed to download photo from Telegram. FileId: $fileId. Error: $err"))

      _ <- ZIO.logInfo(s"Storing photo: ${photoFile.fileName}")
      url <- storage.storePhoto(photoFile.fileName, photoFile.bytes)
        .tapError(err => ZIO.logError(s"Failed to store photo: ${photoFile.fileName}. Error: $err"))
    yield url
