package bot.infrastructure.services.storage

import bot.application.configurations.BotConfig
import zio.{ZIO, ZLayer}

object LocalFileStorageLayer {
  val storageLayer: ZLayer[BotConfig, Throwable, String] =
    ZLayer.fromZIO {
      for {
        config <- ZIO.service[BotConfig]
        localStorage <- ZIO.fromOption(config.localStorage)
          .orElseFail(new RuntimeException("Local storage config is missing"))
      } yield localStorage.path
    }
}
