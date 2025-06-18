package tarot.application.configurations

import zio.Config
import zio.config.magnolia.deriveConfig

final case class PostgresConfig(connectionString: String)

object PostgresConfig {
  implicit val config: Config[PostgresConfig] = deriveConfig[PostgresConfig]
}