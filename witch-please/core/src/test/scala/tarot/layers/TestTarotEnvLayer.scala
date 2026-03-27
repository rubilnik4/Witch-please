package tarot.layers

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import shared.infrastructure.telemetry.StubTelemetryLayer
import shared.infrastructure.telemetry.logging.LoggerLayer
import shared.infrastructure.telemetry.metrics.TelemetryMeterLayer
import shared.infrastructure.telemetry.tracing.TelemetryTracingLayer
import tarot.application.commands.{TarotCommandHandler, TarotCommandHandlerLayer}
import tarot.application.configurations.TarotConfig
import tarot.application.jobs.TarotJobLayer
import tarot.application.queries.{TarotQueryHandler, TarotQueryHandlerLayer}
import tarot.infrastructure.services.TarotService
import tarot.infrastructure.telemetry.TarotTelemetryLayer
import zio.ZLayer
import zio.telemetry.opentelemetry.metrics.Meter
import zio.telemetry.opentelemetry.tracing.Tracing

object TestTarotEnvLayer {
  private val repositoryLayers: ZLayer[TarotConfig, Throwable, TarotService & TarotCommandHandler & TarotQueryHandler] =
    (TestTarotRepositoryLayer.live ++ ZLayer.environment[TarotConfig]) >>>
      (TestTarotServiceLayer.live ++ TarotCommandHandlerLayer.live ++ TarotQueryHandlerLayer.live)

  private val repositoryLayersWithQuill: ZLayer[TarotConfig, Throwable, TarotService & TarotCommandHandler & TarotQueryHandler & Quill.Postgres[SnakeCase]] =
    (TestTarotRepositoryLayer.liveWithQuill ++ ZLayer.environment[TarotConfig]) >>>
      ((TestTarotServiceLayer.live ++ TarotCommandHandlerLayer.live ++ TarotQueryHandlerLayer.live) ++
        ZLayer.service[Quill.Postgres[SnakeCase]])

  private val envLive: ZLayer[TarotConfig & Meter & Tracing, Throwable, TarotEnv] =
    (TelemetryMeterLayer.live ++ TelemetryTracingLayer.live ++ TarotJobLayer.live ++ repositoryLayers) >>>
      TarotEnvLayer.envLive

  private val envWithQuillLive: ZLayer[TarotConfig & Meter & Tracing, Throwable, TarotEnv & Quill.Postgres[SnakeCase]] =
    (TelemetryMeterLayer.live ++ TelemetryTracingLayer.live ++ TarotJobLayer.live ++ repositoryLayersWithQuill) >>>
      (TarotEnvLayer.envLive ++ ZLayer.service[Quill.Postgres[SnakeCase]])

  val testEnvLive: ZLayer[Any, Throwable, TarotEnv] =
    TestTarotConfigLayer.testTarotConfigLive >>>
      (TarotTelemetryLayer.telemetryConfigLayer >>> 
        (StubTelemetryLayer.telemetryLive ++ LoggerLayer.consoleLoggerLive) >>>
        envLive)

  val testEnvWithQuillLive: ZLayer[Any, Throwable, TarotEnv & Quill.Postgres[SnakeCase]] =
    TestTarotConfigLayer.testTarotConfigLive >>>
      (TarotTelemetryLayer.telemetryConfigLayer >>>
        (StubTelemetryLayer.telemetryLive ++ LoggerLayer.consoleLoggerLive) >>>
        envWithQuillLive)
}
