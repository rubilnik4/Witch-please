package tarot.layers

import tarot.application.configurations.AppConfig
import tarot.application.handlers.{SpreadCommandHandler, TarotCommandHandler}
import tarot.application.telemetry.metrics.TarotMeter
import tarot.application.telemetry.tracing.TarotTracing
import tarot.infrastructure.repositories.TarotRepository
import tarot.infrastructure.services.photo.TarotPhotoService

trait AppEnv {
  def appConfig: AppConfig
  def photoService: TarotPhotoService
  def tarotRepository: TarotRepository
  def tarotCommandHandler: TarotCommandHandler
  def tarotMeter: TarotMeter
  def tarotTracing: TarotTracing
}
