package tarot.layers

import tarot.application.configurations.AppConfig
import tarot.application.handlers.TarotCommandHandler
import tarot.application.telemetry.metrics.TarotMeter
import tarot.application.telemetry.tracing.TarotTracing
import tarot.infrastructure.repositories.TarotRepository
import tarot.infrastructure.services.photo.PhotoService
import zio.telemetry.opentelemetry.tracing.Tracing

final case class AppEnvLive (
  appConfig: AppConfig,
  photoService: PhotoService,
  tarotRepository: TarotRepository,
  tarotCommandHandler: TarotCommandHandler,
  tarotMeter: TarotMeter,
  tarotTracing: TarotTracing
) extends AppEnv
