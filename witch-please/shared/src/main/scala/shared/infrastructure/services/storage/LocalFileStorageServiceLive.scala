package shared.infrastructure.services.storage

import shared.models.files.*
import zio.nio.file.{Files, Path}
import zio.stream.ZStream
import zio.{Cause, Chunk, ZIO}

final class LocalFileStorageServiceLive(rootPath: Path) extends FileStorageService:  
  def storePhoto(storedFile: StoredFile): ZIO[Any, Throwable, FileSource] =
    val fullPath = rootPath / storedFile.fileName

    for {
      _ <- ZIO.logDebug(s"[LocalPhotoStorage] Attempting to store photo: ${storedFile.fileName} at path: $fullPath")

      _ <- ZIO
        .fromOption(fullPath.parent)
        .orElseFail(new RuntimeException(s"Cannot determine parent for path: $fullPath"))
        .flatMap { parent =>
          Files.createDirectories(parent)
            .tapError(ex => ZIO.logErrorCause(s"[LocalPhotoStorage] Failed to create parent directories for: $fullPath", Cause.fail(ex)))
            .mapError(ex => new RuntimeException(s"Failed to create directory for $fullPath", ex))
        }
      
      _ <- Files.writeBytes(fullPath, Chunk.fromArray(storedFile.bytes))
        .tapError(ex => ZIO.logErrorCause(s"[LocalPhotoStorage] Failed to store photo: ${storedFile.fileName}", Cause.fail(ex)))
        .mapError(ex => new RuntimeException(s"Failed to write file to $fullPath", ex))
    } yield FileSource.Local(fullPath.toString)

  def getResourcePhoto(resourcePath: String): ZIO[Any, Throwable, StoredFile] =
    for {
      _ <- ZIO.logDebug(s"[LocalPhotoStorage] Attempting to get photo from resource: $resourcePath")

      fileName <- ZIO
        .fromOption(resourcePath.split("/").lastOption)
        .tapError(ex =>
          ZIO.logError(s"Invalid resource path: '$resourcePath' — cannot extract file name"))
        .orElseFail(new RuntimeException(s"Invalid resource path: '$resourcePath' — cannot extract file name"))

      stream <- ZIO.fromOption(Option(getClass.getClassLoader.getResourceAsStream(resourcePath)))
        .tapError(_ =>
          ZIO.logError(s"[LocalPhotoStorage] Resource not found: $resourcePath"))
        .orElseFail(new RuntimeException(s"[LocalPhotoStorage] Resource not found: $resourcePath"))

      photoBytes <- ZStream.fromInputStream(stream).runCollect.map(_.toArray)
        .tapError(ex =>
          ZIO.logErrorCause(s"[LocalPhotoStorage] Could not read stream: $resourcePath", Cause.fail(ex)))
        .mapError(ex => new RuntimeException(s"[LocalPhotoStorage] Could not read stream: $resourcePath", ex))
    } yield StoredFile(fileName, photoBytes)