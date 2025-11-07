package bot.application.configurations

import zio.{Config, Duration}
import zio.config.magnolia.deriveConfig

final case class BotProjectConfig(
  host: String,                            
  port: Int,
  tarotUrl: String,
  userSecretPepper: String,
  maxFutureTime: Duration
)

object BotProjectConfig {
  implicit val config: Config[BotProjectConfig] = deriveConfig[BotProjectConfig]
}
