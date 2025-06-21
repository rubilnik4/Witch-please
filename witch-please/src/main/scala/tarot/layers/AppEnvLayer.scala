package tarot.layers

import tarot.application.configurations.AppConfig
import tarot.application.handlers.TarotCommandHandler
import tarot.application.telemetry.metrics.TarotMeter
import tarot.application.telemetry.tracing.TarotTracing
import tarot.infrastructure.repositories.TarotRepository
import tarot.infrastructure.services.photo.PhotoService
import zio.ZLayer
import zio.telemetry.opentelemetry.tracing.Tracing

object AppEnvLayer {
  val appEnvLive: ZLayer[
    AppConfig
      & PhotoService
      & TarotRepository 
      & TarotCommandHandler
      & TarotMeter & TarotTracing,
    Nothing,
    AppEnv
  ] =
    ZLayer.fromFunction(AppEnvLive.apply)
}
