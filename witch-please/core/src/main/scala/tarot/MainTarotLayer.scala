package tarot

import shared.application.configurations.*
import shared.infrastructure.telemetry.TelemetryLayer
import shared.infrastructure.telemetry.metrics.TelemetryMeterLayer
import shared.infrastructure.telemetry.tracing.TelemetryTracingLayer
import tarot.api.routes.{TarotRoutesLayer, TarotServerLayer}
import tarot.application.commands.TarotCommandHandlerLayer
import tarot.application.configurations.TarotConfig
import tarot.application.queries.TarotQueryHandlerLayer
import tarot.infrastructure.repositories.TarotRepositoryLayer
import tarot.infrastructure.services.TarotServiceLayer
import tarot.infrastructure.telemetry.TarotTelemetryLayer
import tarot.layers.{TarotConfigLayer, TarotEnv, TarotEnvLayer}
import zio.http.Server
import zio.telemetry.opentelemetry.metrics.Meter
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.*

object MainTarotLayer {
  private val envLive: ZLayer[TarotConfig & Meter & Tracing, Throwable, TarotEnv] = {
    val repositoryLayer = TarotRepositoryLayer.tarotRepositoryLive
    val combinedLayers =
      TelemetryMeterLayer.telemetryMeterLive ++
        TelemetryTracingLayer.telemetryTracingLive ++
        repositoryLayer ++ TarotServiceLayer.tarotServiceLive ++
        TarotCommandHandlerLayer.tarotCommandHandlerLive ++ TarotQueryHandlerLayer.tarotQueryHandlerLive
    combinedLayers >>> TarotEnvLayer.envLive
  }

  private val runtimeLive: ZLayer[Any, Throwable, Server] =
    TarotConfigLayer.appConfigLive >>>
      ((TarotTelemetryLayer.telemetryConfigLayer >>> TelemetryLayer.telemetryLive) >>>
        (envLive >>>
          (TarotRoutesLayer.apiRoutesLive >>> TarotServerLayer.serverLive)))

  def run: ZIO[Any, Throwable, Nothing] =
    ZIO.logInfo("Starting witch core application...") *>
      runtimeLive.launch
        .ensuring(ZIO.logInfo("Witch core application stopped"))
        .tapErrorCause(cause => ZIO.logErrorCause("Witch core application failed", cause))
}