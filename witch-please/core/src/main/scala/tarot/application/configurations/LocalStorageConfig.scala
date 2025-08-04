package tarot.application.configurations

import zio.config.magnolia.deriveConfig
import zio.Config

final case class LocalStorageConfig(
  path: String
)

object LocalStorageConfig {
  implicit val config: Config[LocalStorageConfig] = deriveConfig[LocalStorageConfig]
}
