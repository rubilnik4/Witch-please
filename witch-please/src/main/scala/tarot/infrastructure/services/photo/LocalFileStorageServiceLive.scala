package tarot.infrastructure.services.photo

import tarot.domain.models.TarotError
import tarot.domain.models.photo.{PhotoFile, PhotoSource}
import zio.{Cause, Chunk, ZIO}
import zio.nio.file.Files
import zio.nio.file.Path

//import java.nio.file.{Files, Path}

final class LocalFileStorageServiceLive(rootPath: Path) extends FileStorageService:
  
  def storePhoto(photoFile: PhotoFile): ZIO[Any, TarotError, PhotoSource] =
    val fullPath = rootPath / photoFile.fileName

    for {
      _ <- ZIO.logDebug(s"[LocalPhotoStorage] Attempting to store photo: ${photoFile.fileName} at path: $fullPath")

      _ <- Files.writeBytes(fullPath, Chunk.fromArray(photoFile.bytes))
        .tapError(ex => ZIO.logErrorCause(s"[LocalPhotoStorage] Failed to store photo: ${photoFile.fileName}", Cause.fail(ex)))
        .mapError(ex => TarotError.StorageError(s"Failed to write file to $fullPath", ex))
      
      realPath <- fullPath.toRealPath()
    } yield PhotoSource.Local(fullPath.)

  def getResourcePhoto(resourcePath: String): ZIO[Any, TarotError, PhotoFile] =
    for {
      _ <- ZIO.logDebug(s"[LocalPhotoStorage] Attempting to get photo from resource: $resourcePath")

      fileName <- ZIO
        .fromOption(resourcePath.split("/").lastOption)
        .tapError(ex =>
          ZIO.logError(s"Invalid resource path: '$resourcePath' — cannot extract file name"))
        .orElseFail(TarotError.ParsingError(resourcePath, s"Invalid resource path: '$resourcePath' — cannot extract file name"))

      stream <- ZIO.attemptBlockingIO(getClass.getClassLoader.getResourceAsStream(resourcePath))
        .tapError(ex =>
          ZIO.logErrorCause(s"[LocalPhotoStorage] Resource not found: $resourcePath", Cause.fail(ex)))
        .mapError(ex => TarotError.StorageError(s"[LocalPhotoStorage] Resource not found: $resourcePath", ex))

      photoBytes <- ZIO.attemptBlockingIO(stream.readAllBytes())
        .tapError(ex =>
          ZIO.logErrorCause(s"[LocalPhotoStorage] Could not read stream: $resourcePath", Cause.fail(ex)))
        .mapError(ex => TarotError.StorageError(s"[LocalPhotoStorage] Could not read stream: $resourcePath", ex))
    } yield PhotoFile(fileName, photoBytes)