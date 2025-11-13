package tarot.infrastructure.services.photo

import shared.infrastructure.services.storage.FileStorageService
import shared.infrastructure.services.telegram.TelegramApiService
import shared.models.files.{FileSource, StoredFile}
import tarot.domain.models.{TarotError, TarotErrorMapper}
import zio.ZIO

final class PhotoServiceLive(telegram: TelegramApiService, storage: FileStorageService) extends PhotoService:
  def fetchAndStore(fileId: String): ZIO[Any, TarotError, FileSource] =
    for {
      _ <- ZIO.logInfo(s"Downloading photo: $fileId")
      telegramFile <- telegram.downloadPhoto(fileId)
        .tapError(err => ZIO.logError(s"Failed to download photo from Telegram. FileId: $fileId. Error: $err"))
        .mapError(err => TarotErrorMapper.toTarotError("TelegramApiService", err))

      _ <- ZIO.logInfo(s"Storing photo: ${telegramFile.fileName}")
      photoFile = StoredFile(telegramFile.fileName, telegramFile.bytes)
      photoSource <- storage.storePhoto(photoFile)
        .tapError(err => ZIO.logError(s"Failed to store photo: ${photoFile.fileName}. Error: $err"))
        .mapError(err => TarotError.StorageError(err.getMessage, err.getCause))
    } yield photoSource