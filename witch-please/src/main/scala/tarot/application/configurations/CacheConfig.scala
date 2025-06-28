package tarot.application.configurations

import zio.config.magnolia.deriveConfig
import zio.{Config, Duration}

final case class CacheConfig(
  priceExpiration: Duration
)

object CacheConfig {
  implicit val config: Config[CacheConfig] = deriveConfig[CacheConfig]
}
