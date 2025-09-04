package tarot.layers

import tarot.application.commands.TarotCommandHandler
import tarot.application.configurations.TarotConfig
import tarot.application.queries.TarotQueryHandler
import tarot.application.telemetry.metrics.TarotMeter
import tarot.application.telemetry.tracing.TarotTracing
import tarot.infrastructure.repositories.TarotRepository
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.services.TarotService
import zio.ZLayer


object TarotEnvLayer {
  val envLive: ZLayer[
    TarotConfig
      & TarotService
      & TarotRepository 
      & TarotCommandHandler & TarotQueryHandler
      & TarotMeter & TarotTracing,
    Nothing,
    TarotEnv
  ] =
    ZLayer.fromFunction(TarotEnvLive.apply)
}
