package tarot

import tarot.api.routes.{TarotRoutesLayer, TarotServerLayer}
import tarot.application.commands.TarotCommandHandlerLayer
import tarot.application.configurations.TarotConfig
import tarot.application.queries.TarotQueryHandlerLayer
import tarot.application.telemetry.metrics.TarotMeterLayer
import tarot.application.telemetry.tracing.TarotTracingLayer
import tarot.infrastructure.repositories.TarotRepositoryLayer
import tarot.infrastructure.services.TarotServiceLayer
import tarot.infrastructure.telemetry.TelemetryLayer
import tarot.layers.{TarotConfigLayer, TarotEnv, TarotEnvLayer}
import zio.http.Server
import zio.telemetry.opentelemetry.metrics.Meter
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.{ZIO, ZLayer}

object MainAppLayer {
  private val appLive: ZLayer[TarotConfig & Meter & Tracing, Throwable, TarotEnv] = {
    val repositoryLayer = TarotRepositoryLayer.tarotRepositoryLive
    val combinedLayers =
      TarotMeterLayer.tarotMeterLive ++
        TarotTracingLayer.tarotTracingLive ++
        repositoryLayer ++ TarotServiceLayer.tarotServiceLive ++
        TarotCommandHandlerLayer.tarotCommandHandlerLive ++ TarotQueryHandlerLayer.tarotQueryHandlerLive
    combinedLayers >>> TarotEnvLayer.envLive
  }

  private val runtimeLive: ZLayer[Any, Throwable, Server] =
    TarotConfigLayer.appConfigLive >>>
      (TelemetryLayer.telemetryLive >>>
        (appLive >>>
          (TarotRoutesLayer.apiRoutesLive >>> TarotServerLayer.serverLive)))

  def run: ZIO[Any, Throwable, Nothing] =
    ZIO.logInfo("Starting witch core application...") *>
      runtimeLive.launch
        .ensuring(ZIO.logInfo("Witch core application stopped"))
        .tapErrorCause(cause => ZIO.logErrorCause("Witch core application failed", cause))
}