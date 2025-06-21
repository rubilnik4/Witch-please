package tarot.application.configurations

import zio.config.magnolia.deriveConfig
import zio.{Config, Duration}

final case class TelegramConfig(
  token: String
)

object TelegramConfig {
  implicit val config: Config[LocalStorageConfig] = deriveConfig[LocalStorageConfig]
}
