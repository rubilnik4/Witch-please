package bot.application.configurations

import zio.Config
import zio.config.derivation.*
import zio.config.magnolia.deriveConfig

final case class AppConfig(
  project: ProjectConfig,
  telegram: TelegramConfig,
  telemetry: Option[TelemetryConfig]
)

object AppConfig {
  implicit val config: Config[AppConfig] = deriveConfig[AppConfig]
}
