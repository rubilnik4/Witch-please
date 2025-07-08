package tarot.application.configurations

import zio.config.magnolia.deriveConfig
import zio.Config

final case class TelegramConfig(
  chatId: String,                             
  token: String
)

object TelegramConfig {
  implicit val config: Config[LocalStorageConfig] = deriveConfig[LocalStorageConfig]
}
