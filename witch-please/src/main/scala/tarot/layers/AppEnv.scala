package tarot.layers

import tarot.application.handlers.{SpreadCommandHandler, TarotCommandHandler}
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
//  def marketMeter: MarketMeter
//  def marketTracing: MarketTracing
  def photoService: PhotoService
}
