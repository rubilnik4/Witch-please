package tarot.application.configurations

import shared.application.configurations.*
import zio.Config
import zio.config.derivation.*
import zio.config.magnolia.deriveConfig

final case class TarotConfig(
                              project: TarotProjectConfig,
                              jwt: JwtConfig,
                              cache: CacheConfig,
                              telegram: TelegramConfig,
                              localStorage: Option[LocalStorageConfig],
                              postgres: Option[PostgresConfig],
                              telemetry: Option[TelemetryConfig]
)

object TarotConfig {
  implicit val config: Config[TarotConfig] = deriveConfig[TarotConfig]
}
