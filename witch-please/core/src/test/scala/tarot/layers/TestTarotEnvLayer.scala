package tarot.layers

import shared.infrastructure.telemetry.StubTelemetryLayer
import shared.infrastructure.telemetry.logging.LoggerLayer
import shared.infrastructure.telemetry.metrics.TelemetryMeterLayer
import shared.infrastructure.telemetry.tracing.TelemetryTracingLayer
import tarot.application.commands.TarotCommandHandlerLayer
import tarot.application.configurations.TarotConfig
import tarot.application.jobs.TarotJobLayer
import tarot.application.queries.TarotQueryHandlerLayer
import tarot.infrastructure.services.TarotServiceLayer
import tarot.infrastructure.telemetry.TarotTelemetryLayer
import zio.ZLayer
import zio.telemetry.opentelemetry.metrics.Meter
import zio.telemetry.opentelemetry.tracing.Tracing

object TestTarotEnvLayer {
  private val envLive: ZLayer[TarotConfig & Meter & Tracing, Throwable, TarotEnv] = {
    val repositoryLayer = TestTarotRepositoryLayer.postgresTarotRepositoryLive
    val combinedLayers =
      TelemetryMeterLayer.telemetryMeterLive ++ TelemetryTracingLayer.telemetryTracingLive ++
        repositoryLayer ++ TarotServiceLayer.tarotServiceLive ++ TarotJobLayer.tarotJobLive ++
        TarotCommandHandlerLayer.tarotCommandHandlerLive ++ TarotQueryHandlerLayer.tarotQueryHandlerLive
    combinedLayers >>> TarotEnvLayer.envLive
  }

  val testEnvLive: ZLayer[Any, Throwable, TarotEnv] =
    TestTarotConfigLayer.testTarotConfigLive >>>
      (TarotTelemetryLayer.telemetryConfigLayer >>> 
        (StubTelemetryLayer.telemetryLive ++ LoggerLayer.consoleLoggerLive) >>>
        envLive)
}
