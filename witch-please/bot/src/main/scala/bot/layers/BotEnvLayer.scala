package bot.layers

import bot.application.configurations.BotConfig
import bot.application.handlers.*
import bot.application.handlers.telegram.TelegramCommandHandler
import bot.infrastructure.repositories.*
import bot.infrastructure.services.*
import shared.infrastructure.telemetry.metrics.TelemetryMeter
import shared.infrastructure.telemetry.tracing.TelemetryTracing
import zio.ZLayer

object BotEnvLayer {
  val envLive: ZLayer[
    BotConfig
      & BotService
      & BotCommandHandler
      & TelemetryMeter & TelemetryTracing,
    Nothing,
    BotEnv
  ] =
    ZLayer.fromFunction(BotEnvLive.apply)
}
