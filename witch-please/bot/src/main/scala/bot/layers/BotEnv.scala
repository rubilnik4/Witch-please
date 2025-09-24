package bot.layers

import bot.application.configurations.BotConfig
import bot.application.handlers.BotCommandHandler
import bot.application.handlers.telegram.TelegramCommandHandler
import bot.infrastructure.repositories.BotRepository
import bot.infrastructure.services.*
import shared.infrastructure.telemetry.metrics.TelemetryMeter
import shared.infrastructure.telemetry.tracing.TelemetryTracing

trait BotEnv {
  def appConfig: BotConfig
  def botService: BotService
  def botRepository: BotRepository
  def botCommandHandler: BotCommandHandler
  def telemetryMeter: TelemetryMeter
  def telemetryTracing: TelemetryTracing
}
