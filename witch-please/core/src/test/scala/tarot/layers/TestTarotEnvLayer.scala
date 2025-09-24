package tarot.layers

import shared.infrastructure.telemetry.{StubTelemetryLayer, TelemetryLayer}
import shared.infrastructure.telemetry.metrics.TelemetryMeterLayer
import shared.infrastructure.telemetry.tracing.TelemetryTracingLayer
import tarot.MainTarotLayer
import tarot.application.commands.TarotCommandHandlerLayer
import tarot.application.configurations.TarotConfig
import tarot.application.queries.TarotQueryHandlerLayer
import tarot.infrastructure.services.TarotServiceLayer
import zio.ZLayer
import zio.telemetry.opentelemetry.metrics.Meter
import zio.telemetry.opentelemetry.tracing.Tracing

object TestTarotEnvLayer {
  private val envLive: ZLayer[TarotConfig & Meter & Tracing, Throwable, TarotEnv] = {
    val repositoryLayer = TestTarotRepositoryLayer.postgresTarotRepositoryLive
    val combinedLayers =
      TelemetryMeterLayer.telemetryMeterLive ++ TelemetryTracingLayer.telemetryTracingLive ++
        repositoryLayer ++ TarotServiceLayer.tarotServiceLive ++
        TarotCommandHandlerLayer.tarotCommandHandlerLive ++ TarotQueryHandlerLayer.tarotQueryHandlerLive
    combinedLayers >>> TarotEnvLayer.envLive
  }

  val testEnvLive: ZLayer[Any, Throwable, TarotEnv] =
    TestTarotConfigLayer.testTarotConfigLive >>>
      ((MainTarotLayer.telemetryConfigLayer >>> StubTelemetryLayer.telemetryLive) >>>
        envLive)
}
