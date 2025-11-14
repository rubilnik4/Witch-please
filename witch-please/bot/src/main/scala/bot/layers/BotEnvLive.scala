package bot.layers

import bot.application.configurations.BotConfig
import bot.application.handlers.*
import bot.application.handlers.telegram.TelegramCommandHandler
import bot.infrastructure.repositories.*
import bot.infrastructure.services.*
import shared.infrastructure.telemetry.metrics.TelemetryMeter
import shared.infrastructure.telemetry.tracing.TelemetryTracing

final case class BotEnvLive(
  config: BotConfig,
  botService: BotService,
  botCommandHandler: BotCommandHandler,
  telemetryMeter: TelemetryMeter,
  telemetryTracing: TelemetryTracing
) extends BotEnv
