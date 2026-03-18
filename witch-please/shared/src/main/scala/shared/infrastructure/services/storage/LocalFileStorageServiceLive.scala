package shared.infrastructure.services.storage

import shared.models.files.*
import zio.nio.file.{Files, Path}
import zio.{Cause, Chunk, ZIO}

import java.util.UUID

final class LocalFileStorageServiceLive(rootPath: Path) extends FileStorageService:  
  override def storeFile(prefix: String, fileBytes: FileBytes): ZIO[Any, Throwable, FileStored] =
    val id = UUID.randomUUID()
    val fullPath = getFullPath(prefix, id.toString)
    for {
      _ <- ZIO.logDebug(s"Attempting to store file: ${fileBytes.fileName} at path: $fullPath")

      _ <- ZIO
        .fromOption(fullPath.parent)
        .orElseFail(new RuntimeException(s"Cannot determine parent for path: $fullPath"))
        .flatMap { parent =>
          Files.createDirectories(parent)
            .tapError(ex => ZIO.logErrorCause(s"Failed to create parent directories for $fullPath", Cause.fail(ex)))
            .mapError(ex => new RuntimeException(s"Failed to create directory parent for $fullPath", ex))
        }
      
      _ <- Files.writeBytes(fullPath, Chunk.fromArray(fileBytes.bytes))
        .tapError(ex => ZIO.logErrorCause(s"Failed to write file ${fileBytes.fileName} to $fullPath", Cause.fail(ex)))
        .mapError(ex => new RuntimeException(s"Failed to write file ${fileBytes.fileName} to $fullPath", ex))
    } yield FileStored.Local(id, fullPath.toString)

  override def deleteFile(prefix: String, id: UUID): ZIO[Any, Throwable, Boolean] =
    val fullPath = getFullPath(prefix, id.toString)
    for {
      _ <- ZIO.logDebug(s"Attempting to delete file: $id at path: $fullPath")

      deleted <- Files
        .deleteIfExists(fullPath)
        .tapError(ex => ZIO.logErrorCause(s"Failed to delete file $id at path: $fullPath", Cause.fail(ex)))
        .mapError(ex => new RuntimeException(s"Failed to delete file at $fullPath", ex))
    } yield deleted

  override def getFile(prefix: String, id: UUID): ZIO[Any, Throwable, FileBytes] =
    val fullPath = getFullPath(prefix, id.toString)
    for {
      _ <- ZIO.logDebug(s"Attempting to read file: $id at path: $fullPath")
      
      bytes <- Files.readAllBytes(fullPath)
        .tapError(ex => ZIO.logErrorCause(s"Failed to read file $id at path: $fullPath", Cause.fail(ex)))
        .mapError(ex => new RuntimeException(s"Failed to read file at $fullPath", ex))
      fileName = fullPath.filename.toString
    } yield FileBytes(fileName, bytes.toArray)
  
  private def getFullPath(prefix: String, fileName: String) =
    rootPath / sanitizePrefix(prefix) / fileName

  private def sanitizePrefix(prefix: String): String =
    prefix.trim.stripPrefix("/").stripSuffix("/")
