package tarot.layers

import shared.infrastructure.telemetry.metrics.TelemetryMeter
import shared.infrastructure.telemetry.tracing.TelemetryTracing
import tarot.application.commands.TarotCommandHandler
import tarot.application.configurations.TarotConfig
import tarot.application.jobs.TarotJob
import tarot.application.queries.TarotQueryHandler
import tarot.infrastructure.repositories.TarotRepository
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.services.TarotService
import zio.ZLayer


object TarotEnvLayer {
  val envLive: ZLayer[
    TarotConfig
      & TarotService
      & TarotJob
      & TarotRepository 
      & TarotCommandHandler & TarotQueryHandler
      & TelemetryMeter & TelemetryTracing,
    Nothing,
    TarotEnv
  ] =
    ZLayer.fromFunction(TarotEnvLive.apply)
}
