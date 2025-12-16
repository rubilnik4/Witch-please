package bot.layers

import bot.infrastructure.services.tarot.TarotApiUrl
import shared.infrastructure.telemetry.StubTelemetryLayer
import shared.infrastructure.telemetry.logging.LoggerLayer
import shared.infrastructure.telemetry.metrics.TelemetryMeterLayer
import shared.infrastructure.telemetry.tracing.TelemetryTracingLayer
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import tarot.api.routes.TarotRoutesLayer
import tarot.application.commands.*
import tarot.application.configurations.TarotConfig
import tarot.application.jobs.TarotJobLayer
import tarot.application.queries.*
import tarot.infrastructure.services.TarotService
import tarot.infrastructure.telemetry.TarotTelemetryLayer
import tarot.layers.*
import zio.ZLayer
import zio.http.Server
import zio.telemetry.opentelemetry.metrics.Meter
import zio.telemetry.opentelemetry.tracing.Tracing

object TestTarotServerLayer {
  private val repositoryLayers: ZLayer[TarotConfig, Throwable, TarotService & TarotCommandHandler & TarotQueryHandler] =
    (TestTarotRepositoryLayer.live ++ ZLayer.environment[TarotConfig]) >>>
      (TestTarotServiceLayer.live ++ TarotCommandHandlerLayer.live ++ TarotQueryHandlerLayer.live)

  private val envLive: ZLayer[TarotConfig & Meter & Tracing, Throwable, TarotEnv] =
    (TelemetryMeterLayer.live ++ TelemetryTracingLayer.live ++ TarotJobLayer.live ++ repositoryLayers) >>>
      TarotEnvLayer.envLive

  private val testEnvLive: ZLayer[Any, Throwable, TarotEnv] =
    TestTarotConfigLayer.testTarotConfigLive >>>
      (TarotTelemetryLayer.telemetryConfigLayer >>>
        (StubTelemetryLayer.telemetryLive ++ LoggerLayer.consoleLoggerLive) >>>
        envLive)

  val live: ZLayer[Any, Throwable, TarotApiUrl] =
    (Server.defaultWith(_.port(0)) ++ testEnvLive) >>>
      ZLayer.scoped {
        val httpApp = ZioHttpInterpreter().toHttp(TarotRoutesLayer.endpoints)
        for {
          port <- Server.install(httpApp)
          baseUrl = s"http://localhost:$port"
        } yield TarotApiUrl(baseUrl)
      }
}
