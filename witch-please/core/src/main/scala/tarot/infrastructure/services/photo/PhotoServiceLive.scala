package tarot.infrastructure.services.photo

import shared.infrastructure.services.storage.FileStorageService
import shared.infrastructure.services.telegram.TelegramApiService
import shared.models.files.*
import tarot.domain.models.{TarotError, TarotErrorMapper}
import zio.ZIO

final class PhotoServiceLive(telegram: TelegramApiService, storage: FileStorageService) extends PhotoService:
  override def fetchAndStore(fileId: String): ZIO[Any, TarotError, FileStorage] =
    for {
      _ <- ZIO.logInfo(s"Downloading photo: $fileId")
      telegramFile <- telegram.downloadPhoto(fileId)
        .mapError(err => TarotErrorMapper.toTarotError("TelegramApiService", err))

      _ <- ZIO.logInfo(s"Storing photo: ${telegramFile.fileName}")
      photoFile = StoredFile(telegramFile.fileName, telegramFile.bytes)
      photoSource <- storage.storeFile(photoFile)
        .mapError(err => TarotError.StorageError(err.getMessage, err.getCause))
    } yield photoSource