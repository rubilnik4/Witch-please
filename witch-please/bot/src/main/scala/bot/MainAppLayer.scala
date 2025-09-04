package bot

import bot.api.routes.{BotRoutesLayer, BotServerLayer}
import bot.application.configurations.BotConfig
import bot.application.handlers.BotCommandHandlerLayer
import bot.infrastructure.repositories.BotRepositoryLayer
import bot.infrastructure.services.BotServiceLayer
import bot.layers.{BotConfigLayer, BotEnv, BotEnvLayer}
import zio.http.Server
import zio.telemetry.opentelemetry.metrics.Meter
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.{ZIO, ZLayer}

object MainAppLayer {
  private val appLive: ZLayer[BotConfig, Throwable, BotEnv] = {
    val combinedLayers =
      //TarotMeterLayer.tarotMeterLive ++
        //TarotTracingLayer.tarotTracingLive
        BotRepositoryLayer.botRepositoryLive ++ BotServiceLayer.botServiceLive ++
        BotCommandHandlerLayer.botCommandHandlerLive
    combinedLayers >>>
      BotEnvLayer.envLive
  }

  private val runtimeLive: ZLayer[Any, Throwable, Server] =
    BotConfigLayer.configLive >>>
      (appLive >>>
        (BotRoutesLayer.apiRoutesLive >>> BotServerLayer.serverLive))

  def run: ZIO[Any, Throwable, Nothing] =
    ZIO.logInfo("Starting application...") *>
      runtimeLive.launch
        .ensuring(ZIO.logInfo("Application stopped"))
        .tapErrorCause(cause => ZIO.logErrorCause("Application failed", cause))
}