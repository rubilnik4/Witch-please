package bot.application.configurations

import zio.Config
import zio.config.magnolia.deriveConfig

final case class TelegramConfig(
  chatId: String,                             
  token: String
)

object TelegramConfig {
  implicit val config: Config[TelegramConfig] = deriveConfig[TelegramConfig]
}
