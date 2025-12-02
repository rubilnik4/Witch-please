package bot.layers

import bot.application.configurations.BotConfig
import bot.application.handlers.BotCommandHandler
import bot.application.handlers.telegram.TelegramCommandHandler
import bot.infrastructure.services.*
import shared.infrastructure.telemetry.metrics.TelemetryMeter
import shared.infrastructure.telemetry.tracing.TelemetryTracing

trait BotEnv {
  def config: BotConfig
  def services: BotService
  def commandHandlers: BotCommandHandler
  def telemetryMeter: TelemetryMeter
  def telemetryTracing: TelemetryTracing
}
