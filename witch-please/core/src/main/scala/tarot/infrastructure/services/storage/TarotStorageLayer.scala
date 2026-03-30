package tarot.infrastructure.services.storage

import shared.infrastructure.services.storage.{FileStorageService, FileStorageServiceLayer}
import tarot.application.configurations.TarotConfig
import zio.{ZIO, ZLayer}

object TarotStorageLayer {
  val live: ZLayer[TarotConfig, Throwable, FileStorageService] =
    ZLayer.fromZIO {
      ZIO.serviceWithZIO[TarotConfig] { config =>
        config.storage.`type`.trim.toLowerCase match {
          case "local" => localLayer(config)
          case "s3" => s3Layer(config)
          case other =>
            ZIO.fail(new RuntimeException(s"Storage config is invalid: unsupported storage.type '$other', expected 'local' or 's3'"))
        }
      }
    }.flatten

  private def localLayer(config: TarotConfig): ZIO[Any, Throwable, ZLayer[Any, Throwable, FileStorageService]] =
    for {
      localConfig <- ZIO.fromOption(config.storage.local)
        .orElseFail(new RuntimeException("Storage config is invalid: expected 'storage.local' for type=local"))
    } yield ZLayer.succeed(localConfig.path) >>> FileStorageServiceLayer.localLive

  private def s3Layer(config: TarotConfig): ZIO[Any, Throwable, ZLayer[Any, Throwable, FileStorageService]] =
    for {
      s3Config <- ZIO.fromOption(config.storage.s3)
        .orElseFail(new RuntimeException("Storage config is invalid: expected 'storage.s3' for type=s3"))
    } yield ZLayer.succeed(s3Config) >>> FileStorageServiceLayer.s3Live
}
