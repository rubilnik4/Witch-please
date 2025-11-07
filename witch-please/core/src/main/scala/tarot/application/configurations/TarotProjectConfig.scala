package tarot.application.configurations

import zio.{Config, Duration}
import zio.config.magnolia.deriveConfig

final case class TarotProjectConfig(
  host: String,
  port: Int,
  hardPastTime: Duration,
  maxFutureTime: Duration                            
)

object TarotProjectConfig {
  implicit val config: Config[TarotProjectConfig] = deriveConfig[TarotProjectConfig]
}
