package tarot.application.configurations

import zio.{Config, Duration}
import zio.config.magnolia.deriveConfig

final case class TarotProjectConfig(
  host: String,
  port: Int                           
)

object TarotProjectConfig {
  implicit val config: Config[TarotPublishConfig] = deriveConfig[TarotPublishConfig]
}
