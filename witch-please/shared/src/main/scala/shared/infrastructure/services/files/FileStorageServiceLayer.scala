package shared.infrastructure.services.files

import zio.nio.file.Path
import zio.{ZIO, ZLayer}


object FileStorageServiceLayer {
  val localFileStorageServiceLive: ZLayer[String, Throwable, FileStorageService] =
    ZLayer.fromZIO {
      for {
        rawPath <- ZIO.service[String]
        path <-  ZIO.attempt(Path(rawPath))
          .mapError(ex => new RuntimeException(s"Invalid path '$rawPath': ${ex.getMessage}", ex))
      } yield new LocalFileStorageServiceLive(path)
    }
}
