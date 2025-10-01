package bot.application.configurations

import zio.config.magnolia.deriveConfig
import zio.{Config, Duration}

final case class BotProjectConfig(
  host: String,                            
  port: Int,
  tarotUrl: String,
  userSecretPepper: String
)

object BotProjectConfig {
  implicit val config: Config[BotProjectConfig] = deriveConfig[BotProjectConfig]
}
