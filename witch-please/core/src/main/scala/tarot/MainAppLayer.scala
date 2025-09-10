package tarot

import shared.application.configurations.*
import shared.infrastructure.telemetry.TelemetryLayer
import tarot.api.routes.{TarotRoutesLayer, TarotServerLayer}
import tarot.application.commands.TarotCommandHandlerLayer
import tarot.application.configurations.TarotConfig
import tarot.application.queries.TarotQueryHandlerLayer
import tarot.application.telemetry.metrics.TarotMeterLayer
import tarot.application.telemetry.tracing.TarotTracingLayer
import tarot.infrastructure.repositories.TarotRepositoryLayer
import tarot.infrastructure.services.TarotServiceLayer
import tarot.layers.{TarotConfigLayer, TarotEnv, TarotEnvLayer}
import zio.http.Server
import zio.telemetry.opentelemetry.metrics.Meter
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.*

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

  private val telemetryConfigLayer: ZLayer[TarotConfig, Throwable, TelemetryConfig] =
    ZLayer.fromZIO {
      ZIO.serviceWithZIO[TarotConfig] { config =>
        ZIO.fromOption(config.telemetry)
          .orElseFail(new NoSuchElementException("TelemetryConfig is missing in TarotConfig"))
      }
    }

  private val runtimeLive: ZLayer[Any, Throwable, Server] =
    TarotConfigLayer.appConfigLive >>>
      ((telemetryConfigLayer >>> TelemetryLayer.telemetryLive) >>>
        (appLive >>>
          (TarotRoutesLayer.apiRoutesLive >>> TarotServerLayer.serverLive)))

  def run: ZIO[Any, Throwable, Nothing] =
    ZIO.logInfo("Starting witch core application...") *>
      runtimeLive.launch
        .ensuring(ZIO.logInfo("Witch core application stopped"))
        .tapErrorCause(cause => ZIO.logErrorCause("Witch core application failed", cause))
}