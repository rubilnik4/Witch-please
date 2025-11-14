package bot.layers

import bot.application.configurations.BotConfig
import bot.application.handlers.BotCommandHandlerLayer
import bot.infrastructure.telemetry.BotTelemetryLayer
import shared.infrastructure.telemetry.StubTelemetryLayer
import shared.infrastructure.telemetry.logging.LoggerLayer
import shared.infrastructure.telemetry.metrics.TelemetryMeterLayer
import shared.infrastructure.telemetry.tracing.TelemetryTracingLayer
import zio.ZLayer
import zio.telemetry.opentelemetry.metrics.Meter
import zio.telemetry.opentelemetry.tracing.Tracing

object TestBotEnvLayer {
  private val envLive: ZLayer[BotConfig & Meter & Tracing, Throwable, BotEnv] =
    (
      TelemetryMeterLayer.live ++ TelemetryTracingLayer.live ++
      TestBotServiceLayer.botServiceLive ++
      BotCommandHandlerLayer.botCommandHandlerLive
    ) >>> BotEnvLayer.envLive

  val testEnvLive: ZLayer[Any, Throwable, BotEnv] =
    TestBotConfigLayer.testBotConfigLive >>>
      (BotTelemetryLayer.telemetryConfigLayer >>>
        (StubTelemetryLayer.telemetryLive ++ LoggerLayer.consoleLoggerLive) >>>
        envLive)
}
