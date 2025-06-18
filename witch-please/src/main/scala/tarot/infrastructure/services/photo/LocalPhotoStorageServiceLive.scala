package tarot.infrastructure.services.photo

import tarot.domain.models.TarotError
import tarot.domain.models.photo.{PhotoFile, PhotoSource}
import tarot.infrastructure.services.photo.PhotoStorageService
import zio.ZIO

import java.nio.file.{Files, Path, Paths}

final class LocalPhotoStorageServiceLive (rootPath: Path) extends PhotoStorageService:

  def storePhoto(photoFile: PhotoFile): ZIO[Any, TarotError, PhotoSource] =
    val fullPath = rootPath.resolve(photoFile.fileName)

    for {
      _ <- ZIO.logDebug(s"[LocalPhotoStorage] Attempting to store photo: ${photoFile.fileName} at path: $fullPath")

      url <- ZIO
        .attemptBlocking(Files.write(fullPath, photoFile.bytes)).as(s"/files/${photoFile.fileName}")
        .tapError(ex =>
          ZIO.logError(s"[LocalPhotoStorage] Failed to store photo: ${photoFile.fileName}. Exception: ${ex.getMessage}")
        )
        .mapError(ex => TarotError.StorageError(s"Failed to write file to $fullPath", ex))
    } yield PhotoSource.Local(url)
