package bot.application.configurations

import zio.Config
import zio.config.magnolia.deriveConfig

final case class LocalStorageConfig(
  path: String
)

object LocalStorageConfig {
  implicit val config: Config[LocalStorageConfig] = deriveConfig[LocalStorageConfig]
}
