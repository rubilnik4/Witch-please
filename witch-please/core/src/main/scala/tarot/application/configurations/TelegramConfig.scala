package tarot.application.configurations

import zio.config.magnolia.deriveConfig
import zio.Config

final case class TelegramConfig(
  chatId: Long,                             
  token: String
)

object TelegramConfig {
  implicit val config: Config[TelegramConfig] = deriveConfig[TelegramConfig]
}
