package bot

import bot.api.routes.{BotRoutesLayer, BotServerLayer}
import bot.application.configurations.{BotConfig, TelemetryConfig}
import bot.application.handlers.BotCommandHandlerLayer
import bot.infrastructure.repositories.BotRepositoryLayer
import bot.infrastructure.services.BotServiceLayer
import bot.layers.{BotConfigLayer, BotEnv, BotEnvLayer}
import shared.infrastructure.telemetry.TelemetryLayer
import tarot.MainAppLayer.telemetryConfigLayer
import zio.http.Server
import zio.telemetry.opentelemetry.metrics.Meter
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.{ZIO, ZLayer}

object MainAppLayer {
  private val appLive: ZLayer[BotConfig & Meter & Tracing, Throwable, BotEnv] = {
    val combinedLayers =
      TarotMeterLayer.tarotMeterLive ++
        TarotTracingLayer.tarotTracingLive ++
        BotRepositoryLayer.botRepositoryLive ++ BotServiceLayer.botServiceLive ++
        BotCommandHandlerLayer.botCommandHandlerLive
    combinedLayers >>>
      BotEnvLayer.envLive
  }

  private val telemetryConfigLayer: ZLayer[BotConfig, Throwable, TelemetryConfig] =
    ZLayer.fromZIO {
      ZIO.serviceWithZIO[BotConfig] { config =>
        ZIO.fromOption(config.telemetry)
          .orElseFail(new NoSuchElementException("TelemetryConfig is missing in BotConfig"))
      }
    }

  private val runtimeLive: ZLayer[Any, Throwable, Server] =
    BotConfigLayer.configLive >>>
      ((telemetryConfigLayer >>> TelemetryLayer.telemetryLive) >>>
        (appLive >>>
          (BotRoutesLayer.apiRoutesLive >>> BotServerLayer.serverLive)))

  def run: ZIO[Any, Throwable, Nothing] =
    ZIO.logInfo("Starting witch bot application...") *>
      runtimeLive.launch
        .ensuring(ZIO.logInfo("Witch bot application stopped"))
        .tapErrorCause(cause => ZIO.logErrorCause("Witch bot application failed", cause))
}