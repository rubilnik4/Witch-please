package shared.infrastructure.services.storage

import shared.models.files.FileBytes
import zio.stream.ZStream
import zio.{Cause, ZIO}

final class ResourceFileServiceLive extends ResourceFileService:
  override def getResourceFile(resourcePath: String): ZIO[Any, Throwable, FileBytes] =
    for {
      _ <- ZIO.logDebug(s"Attempting to get photo from resource: $resourcePath")

      fileName <- ZIO
        .fromOption(resourcePath.split("/").lastOption)
        .tapError(_ => ZIO.logError(s"Invalid resource path: '$resourcePath' - cannot extract file name"))
        .orElseFail(new RuntimeException(s"Invalid resource path: '$resourcePath' - cannot extract file name"))

      stream <- ZIO.fromOption(Option(getClass.getClassLoader.getResourceAsStream(resourcePath)))
        .tapError(_ => ZIO.logError(s"Resource not found: $resourcePath"))
        .orElseFail(new RuntimeException(s"Resource not found: $resourcePath"))

      bytes <- ZStream.fromInputStream(stream).runCollect.map(_.toArray)
        .tapError(ex => ZIO.logErrorCause(s"Could not read stream: $resourcePath", Cause.fail(ex)))
        .mapError(ex => new RuntimeException(s"Could not read stream: $resourcePath", ex))
    } yield FileBytes(fileName, bytes)
