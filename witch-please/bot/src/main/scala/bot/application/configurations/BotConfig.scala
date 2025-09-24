package bot.application.configurations

import shared.application.configurations.TelemetryConfig
import tarot.application.configurations.LocalStorageConfig
import zio.Config
import zio.config.derivation.*
import zio.config.magnolia.deriveConfig

final case class BotConfig(
  project: ProjectConfig,
  telegram: TelegramConfig,
  localStorage: Option[LocalStorageConfig],
  telemetry: Option[TelemetryConfig]
)

object BotConfig {
  implicit val config: Config[BotConfig] = deriveConfig[BotConfig]
}
