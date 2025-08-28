package bot

import bot.api.routes.{BotRoutesLayer, BotServerLayer}
import bot.application.configurations.AppConfig
import bot.application.handlers.BotCommandHandlerLayer
import bot.infrastructure.repositories.BotRepositoryLayer
import bot.infrastructure.services.BotServiceLayer
import bot.layers.{AppConfigLayer, AppEnv, AppEnvLayer}
import zio.http.Server
import zio.telemetry.opentelemetry.metrics.Meter
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.{ZIO, ZLayer}

object MainAppLayer {
  private val appLive: ZLayer[AppConfig, Throwable, AppEnv] = {
    val combinedLayers =
      //TarotMeterLayer.tarotMeterLive ++
        //TarotTracingLayer.tarotTracingLive
        BotRepositoryLayer.botRepositoryLive ++ BotServiceLayer.botServiceLive ++
        BotCommandHandlerLayer.botCommandHandlerLive
    combinedLayers >>>
      AppEnvLayer.appEnvLive
  }

  private val runtimeLive: ZLayer[Any, Throwable, Server] =
    AppConfigLayer.appConfigLive >>>
      (appLive >>>
        (BotRoutesLayer.apiRoutesLive >>> BotServerLayer.serverLive))

  def run: ZIO[Any, Throwable, Nothing] =
    ZIO.logInfo("Starting application...") *>
      runtimeLive.launch
        .ensuring(ZIO.logInfo("Application stopped"))
        .tapErrorCause(cause => ZIO.logErrorCause("Application failed", cause))
}