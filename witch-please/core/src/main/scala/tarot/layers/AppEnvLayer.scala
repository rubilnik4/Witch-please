package tarot.layers

import tarot.application.commands.TarotCommandHandler
import tarot.application.configurations.AppConfig
import tarot.application.queries.TarotQueryHandler
import tarot.application.telemetry.metrics.TarotMeter
import tarot.application.telemetry.tracing.TarotTracing
import tarot.infrastructure.repositories.TarotRepository
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.services.TarotService
import zio.ZLayer


object AppEnvLayer {
  val appEnvLive: ZLayer[
    AppConfig
      & TarotService
      & TarotRepository 
      & TarotCommandHandler & TarotQueryHandler
      & TarotMeter & TarotTracing,
    Nothing,
    AppEnv
  ] =
    ZLayer.fromFunction(AppEnvLive.apply)
}
