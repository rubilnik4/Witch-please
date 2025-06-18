package tarot.application.configurations

import zio.Config
import zio.config.derivation.*
import zio.config.magnolia.deriveConfig

final case class AppConfig(
  project: ProjectConfig,
  cache: CacheConfig,
  postgres: Option[PostgresConfig],
  telemetry: Option[TelemetryConfig]
)

object AppConfig {
  implicit val config: Config[AppConfig] = deriveConfig[AppConfig]
}
