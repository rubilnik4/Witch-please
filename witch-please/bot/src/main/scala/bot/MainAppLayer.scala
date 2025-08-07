package bot

import bot.application.configurations.AppConfig
import bot.layers.{AppConfigLayer, AppEnv, AppEnvLayer}
import zio.http.Server
import zio.telemetry.opentelemetry.metrics.Meter
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.{ZIO, ZLayer}

object MainAppLayer {
  private val appLive: ZLayer[AppConfig & Meter & Tracing, Throwable, AppEnv] = {
    //val repositoryLayer = TarotRepositoryLayer.tarotRepositoryLive
    //val combinedLayers =
      //TarotMeterLayer.tarotMeterLive ++
        //TarotTracingLayer.tarotTracingLive
        //repositoryLayer ++ TarotServiceLayer.tarotServiceLive ++
        //TarotCommandHandlerLayer.tarotCommandHandlerLive
    //combinedLayers >>> 
    AppEnvLayer.appEnvLive
  }

  private val runtimeLive: ZLayer[Any, Throwable, Server] =
    AppConfigLayer.appConfigLive >>>
      //(TelemetryLayer.telemetryLive >>>
        (appLive >>>
          (RoutesLayer.apiRoutesLive >>> ServerLayer.serverLive))
       // )

  def run: ZIO[Any, Throwable, Nothing] =
    ZIO.logInfo("Starting application...") *>
      runtimeLive.launch
        .ensuring(ZIO.logInfo("Application stopped"))
        .tapErrorCause(cause => ZIO.logErrorCause("Application failed", cause))
}