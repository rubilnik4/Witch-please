package tarot.infrastructure.services.storage

import tarot.application.configurations.TarotConfig
import zio.{ZIO, ZLayer}

object LocalFileStorageLayer {
  val storageLayer: ZLayer[TarotConfig, Throwable, String] =
    ZLayer.fromZIO {
      for {
        config <- ZIO.service[TarotConfig]
        localStorage <- ZIO.fromOption(config.localStorage)
          .orElseFail(new RuntimeException("Local storage config is missing"))
      } yield localStorage.path
    }
}
