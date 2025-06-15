package tarot.layers

trait AppEnv {
  def appConfig: AppConfig
  def marketCache: MarketCache
  def marketRepository: MarketRepository
  def marketData: MarketData
  def marketApi: MarketApi
  def marketQueryHandler: MarketQueryHandler
  def marketCommandHandler: MarketCommandHandler
  def marketMeter: MarketMeter
  def marketTracing: MarketTracing
}
