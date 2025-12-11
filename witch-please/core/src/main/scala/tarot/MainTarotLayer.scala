package tarot

import shared.application.configurations.TelemetryConfig
import shared.infrastructure.telemetry.TelemetryLayer
import shared.infrastructure.telemetry.metrics.TelemetryMeterLayer
import shared.infrastructure.telemetry.tracing.TelemetryTracingLayer
import tarot.api.routes.{TarotRoutesLayer, TarotServerLayer}
import tarot.application.commands.{TarotCommandHandler, TarotCommandHandlerLayer}
import tarot.application.configurations.TarotConfig
import tarot.application.jobs.TarotJobLayer
import tarot.application.queries.{TarotQueryHandler, TarotQueryHandlerLayer}
import tarot.infrastructure.repositories.TarotRepositoryLayer
import tarot.infrastructure.repositories.TarotRepositoryLayer.Repositories
import tarot.infrastructure.services.{TarotService, TarotServiceLayer}
import tarot.infrastructure.telemetry.TarotTelemetryLayer
import tarot.layers.{TarotConfigLayer, TarotEnv, TarotEnvLayer}
import zio.{ZLayer, *}
import zio.http.Server
import zio.telemetry.opentelemetry.metrics.Meter
import zio.telemetry.opentelemetry.tracing.Tracing

object MainTarotLayer {
  private val repositoryLayers =
    (TarotRepositoryLayer.live ++ ZLayer.environment[TarotConfig]) >>>
      (TarotServiceLayer.live ++ TarotCommandHandlerLayer.live ++ TarotQueryHandlerLayer.live)

  private val envLive: ZLayer[TarotConfig & Meter & Tracing, Throwable, TarotEnv] =
    (TelemetryMeterLayer.live ++ TelemetryTracingLayer.live ++ TarotJobLayer.live ++ repositoryLayers) >>>
      TarotEnvLayer.envLive

  private val runtimeLive: ZLayer[Any, Throwable, Server] =
    TarotConfigLayer.appConfigLive >>> 
      (TarotTelemetryLayer.telemetryConfigLayer >>> TelemetryLayer.telemetryLive >>>
        (envLive >>>
          (TarotRoutesLayer.live >>> TarotServerLayer.live)))

  def run: ZIO[Any, Throwable, Nothing] =
    ZIO.logInfo("Starting witch core application...") *>
      runtimeLive.launch
        .ensuring(ZIO.logInfo("Witch core application stopped"))
        .tapErrorCause(cause => ZIO.logErrorCause("Witch core application failed", cause))
}