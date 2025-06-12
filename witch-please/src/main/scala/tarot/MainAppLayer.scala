package tarot

object MainAppLayer {
//  private val appLive: ZLayer[AppConfig & Meter & Tracing, Throwable, AppEnv] = {
//    val repositoryLayer = PostgresMarketRepositoryLayer.postgresMarketRepositoryLive
//    val cacheLayer = repositoryLayer >>> MarketCacheLayer.marketCacheLive
//    val combinedLayers =
//      MarketMeterLayer.marketMeterLive ++
//        MarketTracingLayer.marketTracingLive ++
//        repositoryLayer ++
//        cacheLayer ++
//        BinanceMarketApiLayer.binanceMarketApiLive ++
//        MarketDataLayer.marketDataLive ++
//        MarketQueryHandlerLayer.marketQueryHandlerLive ++
//        MarketCommandHandlerLayer.marketCommandHandlerLive
//    combinedLayers >>> AppEnvLayer.appEnvLive
//  }
//
//  private val runtimeLive: ZLayer[Any, Throwable, Server] =
//    AppConfigLayer.appConfigLive >>>
//      (TelemetryLayer.telemetryLive >>>
//        (appLive >>>
//          (RoutesLayer.apiRoutesLive >>> ServerLayer.serverLive) ++ SpreadJobLayer.spreadJobLive))
//
//  def run: ZIO[Any, Throwable, Nothing] =
//    ZIO.logInfo("Starting application...") *>
//      runtimeLive.launch
//        .ensuring(ZIO.logInfo("Application stopped"))
//        .tapErrorCause(cause => ZIO.logErrorCause("Application failed", cause))
}