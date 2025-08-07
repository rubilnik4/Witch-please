package tarot

import tarot.api.routes.{RoutesLayer, ServerLayer}
import tarot.application.configurations.AppConfig
import tarot.application.handlers.TarotCommandHandlerLayer
import tarot.application.telemetry.metrics.TarotMeterLayer
import tarot.application.telemetry.tracing.TarotTracingLayer
import tarot.infrastructure.repositories.TarotRepositoryLayer
import tarot.infrastructure.services.TarotServiceLayer
import tarot.infrastructure.telemetry.TelemetryLayer
import tarot.layers.{AppConfigLayer, AppEnv, AppEnvLayer}
import zio.http.Server
import zio.telemetry.opentelemetry.metrics.Meter
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.{ZIO, ZLayer}

object MainAppLayer {
  private val appLive: ZLayer[AppConfig & Meter & Tracing, Throwable, AppEnv] = {
    val repositoryLayer = TarotRepositoryLayer.tarotRepositoryLive
    val combinedLayers =
      TarotMeterLayer.tarotMeterLive ++
        TarotTracingLayer.tarotTracingLive ++
        repositoryLayer ++ TarotServiceLayer.tarotServiceLive ++
        TarotCommandHandlerLayer.tarotCommandHandlerLive
    combinedLayers >>> AppEnvLayer.appEnvLive
  }

  private val runtimeLive: ZLayer[Any, Throwable, Server] =
    AppConfigLayer.appConfigLive >>>
      (TelemetryLayer.telemetryLive >>>
        (appLive >>>
          (RoutesLayer.apiRoutesLive >>> ServerLayer.serverLive)))

  def run: ZIO[Any, Throwable, Nothing] =
    ZIO.logInfo("Starting application...") *>
      runtimeLive.launch
        .ensuring(ZIO.logInfo("Application stopped"))
        .tapErrorCause(cause => ZIO.logErrorCause("Application failed", cause))
}