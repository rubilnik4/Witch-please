package bot

import bot.api.routes.*
import bot.application.configurations.*
import bot.application.handlers.BotCommandHandlerLayer
import bot.infrastructure.repositories.BotRepositoryLayer
import bot.infrastructure.services.BotServiceLayer
import bot.layers.{BotConfigLayer, BotEnv, BotEnvLayer}
import shared.application.configurations.TelemetryConfig
import shared.infrastructure.telemetry.TelemetryLayer
import shared.infrastructure.telemetry.metrics.*
import shared.infrastructure.telemetry.tracing.*
import tarot.MainAppLayer.telemetryConfigLayer
import zio.http.Server
import zio.telemetry.opentelemetry.metrics.Meter
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.{ZIO, ZLayer}

object MainAppLayer {
  private val envLive: ZLayer[BotConfig & Meter & Tracing, Throwable, BotEnv] = {
    val combinedLayers =
      TelemetryMeterLayer.telemetryMeterLive ++
        TelemetryTracingLayer.telemetryTracingLive ++
        BotRepositoryLayer.botRepositoryLive ++ BotServiceLayer.botServiceLive ++
        BotCommandHandlerLayer.botCommandHandlerLive
    combinedLayers >>> BotEnvLayer.envLive
  }

  val telemetryConfigLayer: ZLayer[BotConfig, Throwable, TelemetryConfig] =
    ZLayer.fromZIO {
      ZIO.serviceWithZIO[BotConfig] { config =>
        ZIO.fromOption(config.telemetry)
          .orElseFail(new NoSuchElementException("TelemetryConfig is missing in BotConfig"))
      }
    }

  private val runtimeLive: ZLayer[Any, Throwable, Server] =
    BotConfigLayer.configLive >>>
      ((telemetryConfigLayer >>> TelemetryLayer.telemetryLive) >>>
        (envLive >>>
          (BotRoutesLayer.apiRoutesLive >>> BotServerLayer.serverLive)))

  def run: ZIO[Any, Throwable, Nothing] =
    ZIO.logInfo("Starting witch bot application...") *>
      runtimeLive.launch
        .ensuring(ZIO.logInfo("Witch bot application stopped"))
        .tapErrorCause(cause => ZIO.logErrorCause("Witch bot application failed", cause))
}