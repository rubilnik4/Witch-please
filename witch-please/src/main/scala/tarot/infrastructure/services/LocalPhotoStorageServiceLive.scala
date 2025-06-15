package tarot.infrastructure.services

import tarot.domain.models.TarotError
import zio.ZIO
import zio.http.Path

import java.nio.file.Files

final class LocalPhotoStorageServiceLive (rootPath: Path) extends PhotoStorageService:

  def storePhoto(fileName: String, bytes: Array[Byte]): ZIO[Any, TarotError, String] =
    val fullPath = rootPath.resolve(fileName)

    ZIO
      .attempt(Files.write(fullPath, bytes))
      .map(_ => s"/files/$fileName")
      .mapError { ex =>
        TarotError.StorageError(s"Failed to write file to $fullPath", ex)
      }
