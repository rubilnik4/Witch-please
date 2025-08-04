package tarot.layers

import tarot.application.configurations.AppConfig
import tarot.application.handlers.TarotCommandHandler
import tarot.application.telemetry.metrics.TarotMeter
import tarot.application.telemetry.tracing.TarotTracing
import tarot.infrastructure.repositories.TarotRepository
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.services.TarotService


final case class AppEnvLive (
  appConfig: AppConfig,
  tarotService: TarotService,
  tarotRepository: TarotRepository,
  tarotCommandHandler: TarotCommandHandler,
  tarotMeter: TarotMeter,
  tarotTracing: TarotTracing
) extends AppEnv
