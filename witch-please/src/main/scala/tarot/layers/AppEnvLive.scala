package tarot.layers

import zio.telemetry.opentelemetry.tracing.Tracing

final case class AppEnvLive (
  appConfig: AppConfig,
  marketCache: MarketCache,
  marketRepository: MarketRepository,
  marketData: MarketData,
  marketApi: MarketApi,
  marketQueryHandler: MarketQueryHandler,
  marketCommandHandler: MarketCommandHandler,
  marketMeter: MarketMeter,
  marketTracing: MarketTracing
) extends AppEnv
