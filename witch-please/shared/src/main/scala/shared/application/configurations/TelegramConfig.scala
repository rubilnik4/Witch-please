package shared.application.configurations

import zio.Config
import zio.config.magnolia.deriveConfig

final case class TelegramConfig(
  chatId: Option[Long] = None,
  token: String,
  secret: String,                              
  baseUrl: String
)

object TelegramConfig {
  implicit val config: Config[TelegramConfig] = deriveConfig[TelegramConfig]
}
