package tarot.layers

import tarot.application.configurations.AppConfig
import tarot.application.handlers.TarotCommandHandler
import tarot.application.telemetry.metrics.TarotMeter
import tarot.application.telemetry.tracing.TarotTracing
import tarot.infrastructure.repositories.TarotRepository
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.services.TarotService


trait AppEnv {
  def appConfig: AppConfig
  def tarotService: TarotService
  def tarotRepository: TarotRepository
  def tarotCommandHandler: TarotCommandHandler
  def tarotMeter: TarotMeter
  def tarotTracing: TarotTracing
}
