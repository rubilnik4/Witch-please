package tarot.application.configurations

import shared.application.configurations.{LocalStorageConfig, S3StorageConfig}
import zio.Config
import zio.config.magnolia.deriveConfig

final case class TarotStorageConfig(
  `type`: String,
  local: Option[LocalStorageConfig],
  s3: Option[S3StorageConfig]
)

object TarotStorageConfig {
  implicit val config: Config[TarotStorageConfig] = deriveConfig[TarotStorageConfig]
}
