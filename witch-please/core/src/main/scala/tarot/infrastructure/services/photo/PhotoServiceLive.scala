package tarot.infrastructure.services.photo

import shared.infrastructure.services.telegram.TelegramApiService
import tarot.domain.models.{TarotError, TarotErrorMapper}
import tarot.domain.models.photo.{PhotoFile, PhotoSource}
import zio.ZIO

final class PhotoServiceLive(telegram: TelegramApiService, storage: FileStorageService)
  extends PhotoService:

  def fetchAndStore(fileId: String): ZIO[Any, TarotError, PhotoSource] =
    for {
      _ <- ZIO.logInfo(s"Downloading photo: $fileId")
      telegramFile <- telegram.downloadPhoto(fileId)
        .tapError(err => ZIO.logError(s"Failed to download photo from Telegram. FileId: $fileId. Error: $err"))
        .mapError(err => TarotErrorMapper.toTarotError("TelegramApiService", err))

      _ <- ZIO.logInfo(s"Storing photo: ${telegramFile.fileName}")
      photoFile = PhotoFile(telegramFile.fileName, telegramFile.bytes)
      photoSource <- storage.storePhoto(photoFile)
        .tapError(err => ZIO.logError(s"Failed to store photo: ${photoFile.fileName}. Error: $err"))
    } yield photoSource