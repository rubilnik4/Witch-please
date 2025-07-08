package tarot.layers

import tarot.application.configurations.AppConfig
import tarot.application.handlers.TarotCommandHandler
import tarot.application.telemetry.metrics.TarotMeter
import tarot.application.telemetry.tracing.TarotTracing
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.services.TarotService

import zio.ZLayer


object AppEnvLayer {
  val appEnvLive: ZLayer[
    AppConfig
      & TarotService
      & SpreadRepository 
      & TarotCommandHandler
      & TarotMeter & TarotTracing,
    Nothing,
    AppEnv
  ] =
    ZLayer.fromFunction(AppEnvLive.apply)
}
