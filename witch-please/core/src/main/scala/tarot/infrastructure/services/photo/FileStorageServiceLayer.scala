package tarot.infrastructure.services.photo

import tarot.application.configurations.AppConfig
import zio.{ZIO, ZLayer}
import zio.nio.file.Path


object FileStorageServiceLayer {
  val localFileStorageServiceLive: ZLayer[AppConfig, Throwable, FileStorageService] =
    ZLayer.fromZIO {
      for {
        config <- ZIO.service[AppConfig]

        localStorageConfig <- ZIO.fromOption(config.localStorage)
          .tapError(_ => ZIO.logError("Missing local storage config"))
          .orElseFail(new RuntimeException("Local storage config is missing"))

        path <-  ZIO.attempt(Path(localStorageConfig.path))
          .mapError(ex => new RuntimeException(s"Invalid path '${localStorageConfig.path}': ${ex.getMessage}", ex))

      } yield new LocalFileStorageServiceLive(path)
    }
}
