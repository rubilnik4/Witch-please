package tarot.application.configurations

import zio.Config
import zio.config.magnolia.deriveConfig

final case class JwtConfig(
  secretKey: String,
  expirationMinutes: Int
)

object JwtConfig {
  implicit val config: Config[JwtConfig] = deriveConfig[JwtConfig]
}
