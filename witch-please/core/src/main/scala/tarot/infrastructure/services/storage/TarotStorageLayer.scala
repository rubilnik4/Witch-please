package tarot.infrastructure.services.storage

import shared.application.configurations.S3StorageConfig
import shared.infrastructure.services.storage.{FileStorageService, FileStorageServiceLayer}
import tarot.application.configurations.{LocalStorageConfig, TarotConfig}
import zio.{ZIO, ZLayer}

sealed trait TarotStorageConfig

object TarotStorageLayer {
  private final case class Local(config: LocalStorageConfig) extends TarotStorageConfig
  private final case class S3(config: S3StorageConfig) extends TarotStorageConfig

  private val configLayer: ZLayer[TarotConfig, Throwable, TarotStorageConfig] =
    ZLayer.fromZIO {
      ZIO.serviceWithZIO[TarotConfig] { config =>
        (config.localStorage, config.s3Storage) match {
          case (Some(localConfig), None) =>
            ZIO.succeed(Local(localConfig))
          case (None, Some(s3Config)) =>
            ZIO.succeed(S3(s3Config))
          case (None, None) =>
            ZIO.fail(new RuntimeException("Storage config is missing: expected exactly one of 'localStorage' or 's3Storage'"))
          case (Some(_), Some(_)) =>
            ZIO.fail(new RuntimeException("Storage config is invalid: only one of 'localStorage' or 's3Storage' can be defined"))
        }
      }
    }

  val live: ZLayer[TarotConfig, Throwable, FileStorageService] =
    (configLayer >>> ZLayer.fromZIO {
      ZIO.serviceWith[TarotStorageConfig] {
        case Local(config) =>
          ZLayer.succeed(config.path) >>> FileStorageServiceLayer.localLive
        case S3(config) =>
          ZLayer.succeed(config) >>> FileStorageServiceLayer.s3Live
      }
    }).flatten
}
