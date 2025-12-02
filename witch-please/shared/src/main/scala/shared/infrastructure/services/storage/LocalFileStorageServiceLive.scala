package shared.infrastructure.services.storage

import shared.models.files.*
import zio.nio.file.{Files, Path}
import zio.stream.ZStream
import zio.{Cause, Chunk, ZIO}

import java.util.UUID

final class LocalFileStorageServiceLive(rootPath: Path) extends FileStorageService:  
  override def storeFile(storedFile: StoredFile): ZIO[Any, Throwable, FileStorage] =
    val id = UUID.randomUUID()
    val fullPath = getFullPath(id.toString)
    for {
      _ <- ZIO.logDebug(s"Attempting to store file: ${storedFile.fileName} at path: $fullPath")

      _ <- ZIO
        .fromOption(fullPath.parent)
        .orElseFail(new RuntimeException(s"Cannot determine parent for path: $fullPath"))
        .flatMap { parent =>
          Files.createDirectories(parent)
            .tapError(ex => ZIO.logErrorCause(s"Failed to create parent directories for $fullPath", Cause.fail(ex)))
            .mapError(ex => new RuntimeException(s"Failed to create directory parent for $fullPath", ex))
        }
      
      _ <- Files.writeBytes(fullPath, Chunk.fromArray(storedFile.bytes))
        .tapError(ex => ZIO.logErrorCause(s"Failed to write file ${storedFile.fileName} to $fullPath", Cause.fail(ex)))
        .mapError(ex => new RuntimeException(s"Failed to write file ${storedFile.fileName} to $fullPath", ex))
    } yield FileStorage.Local(id, fullPath.toString)

  override def deleteFile(id: UUID): ZIO[Any, Throwable, Boolean] =
    val fullPath = getFullPath(id.toString)
    for {
      _ <- ZIO.logDebug(s"Attempting to delete file: $id at path: $fullPath")

      deleted <- Files
        .deleteIfExists(fullPath)
        .tapError(ex => ZIO.logErrorCause(s"Failed to delete file $id at path: $fullPath", Cause.fail(ex)))
        .mapError(ex => new RuntimeException(s"Failed to delete file at $fullPath", ex))
    } yield deleted
    
  override def getResourceFile(resourcePath: String): ZIO[Any, Throwable, StoredFile] =
    for {
      _ <- ZIO.logDebug(s"Attempting to get photo from resource: $resourcePath")

      fileName <- ZIO
        .fromOption(resourcePath.split("/").lastOption)
        .tapError(ex =>
          ZIO.logError(s"Invalid resource path: '$resourcePath' — cannot extract file name"))
        .orElseFail(new RuntimeException(s"Invalid resource path: '$resourcePath' — cannot extract file name"))

      stream <- ZIO.fromOption(Option(getClass.getClassLoader.getResourceAsStream(resourcePath)))
        .tapError(_ =>
          ZIO.logError(s"Resource not found: $resourcePath"))
        .orElseFail(new RuntimeException(s"Resource not found: $resourcePath"))

      bytes <- ZStream.fromInputStream(stream).runCollect.map(_.toArray)
        .tapError(ex =>
          ZIO.logErrorCause(s"Could not read stream: $resourcePath", Cause.fail(ex)))
        .mapError(ex => new RuntimeException(s"Could not read stream: $resourcePath", ex))
    } yield StoredFile(fileName, bytes)
  
  private def getFullPath(fileName: String) =
    rootPath / fileName