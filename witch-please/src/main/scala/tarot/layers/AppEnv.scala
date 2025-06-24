package tarot.layers

import tarot.application.configurations.AppConfig
import tarot.application.handlers.{SpreadCommandHandler, TarotCommandHandler}
import tarot.application.telemetry.metrics.TarotMeter
import tarot.application.telemetry.tracing.TarotTracing
import tarot.infrastructure.repositories.TarotRepository
import tarot.infrastructure.services.TarotService
import tarot.infrastructure.services.photo.PhotoService

trait AppEnv {
  def appConfig: AppConfig
  def tarotService: TarotService
  def tarotRepository: TarotRepository
  def tarotCommandHandler: TarotCommandHandler
  def tarotMeter: TarotMeter
  def tarotTracing: TarotTracing
}
