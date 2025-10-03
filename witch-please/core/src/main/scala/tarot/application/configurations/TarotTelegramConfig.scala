package tarot.application.configurations

import zio.Config
import zio.config.magnolia.deriveConfig

final case class TarotTelegramConfig(
  chatId: Option[Long] = None,
  token: String
)

object TarotTelegramConfig {
  implicit val config: Config[TarotTelegramConfig] = deriveConfig[TarotTelegramConfig]
}
