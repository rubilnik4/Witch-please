package tarot.infrastructure.services.photo

import tarot.domain.models.TarotError
import tarot.domain.models.photo.PhotoFile
import tarot.infrastructure.services.photo.TelegramPhotoDownloader
import zio.{Ref, ZIO}

final class FakeTelegramPhotoDownloader(downloaded: Ref[Vector[String]]) extends TelegramPhotoDownloader:
  override def download(fileId: String): ZIO[Any, TarotError, PhotoFile] =
    for {
      _ <- ZIO.logDebug(s"[FakeTelegramPhotoDownloader] Attempting to download photo: $fileId")
      _     <- downloaded.update(_ :+ fileId)
      bytes <- ZIO.succeed(s"image-$fileId".getBytes())
    } yield PhotoFile(s"$fileId.jpg", bytes)
