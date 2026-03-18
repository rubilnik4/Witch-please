package shared.application.configurations

import zio.Config
import zio.config.magnolia.deriveConfig

final case class S3StorageConfig(
  endpoint: String,
  region: String,
  bucket: String,
  accessKeyId: String,
  secretAccessKey: String,
  keyPrefix: Option[String] = None
)

object S3StorageConfig {
  implicit val config: Config[S3StorageConfig] = deriveConfig[S3StorageConfig]
}
