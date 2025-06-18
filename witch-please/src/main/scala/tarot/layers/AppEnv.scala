package tarot.layers

import tarot.application.handlers.{SpreadCommandHandler, TarotCommandHandler}
import tarot.application.telemetry.metrics.TarotMeter
import tarot.application.telemetry.tracing.TarotTracing
import tarot.infrastructure.repositories.TarotRepository
import tarot.infrastructure.services.photo.PhotoService

trait AppEnv {
//  def appConfig: AppConfig
//  def marketCache: MarketCache
  def tarotRepository: TarotRepository
//  def marketData: MarketData
//  def marketApi: MarketApi
//  def marketQueryHandler: MarketQueryHandler
  def tarotCommandHandler: TarotCommandHandler
  def tarotMeter: TarotMeter
  def tarotTracing: TarotTracing
  def photoService: PhotoService
}
