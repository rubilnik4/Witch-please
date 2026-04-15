package bot.application.configurations

import shared.application.configurations.{LocalStorageConfig, TelegramConfig, TelemetryConfig}
import zio.Config
import zio.config.derivation.*
import zio.config.magnolia.deriveConfig

final case class BotConfig(
  project: BotProjectConfig,
  telegram: TelegramConfig,
  localStorage: Option[LocalStorageConfig],
  telemetry: Option[TelemetryConfig]
)

object BotConfig {
  implicit val config: Config[BotConfig] = deriveConfig[BotConfig]
}
