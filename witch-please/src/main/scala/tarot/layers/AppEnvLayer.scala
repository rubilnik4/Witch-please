package tarot.layers

import zio.ZLayer
import zio.telemetry.opentelemetry.tracing.Tracing

object AppEnvLayer {
  val appEnvLive: ZLayer[
    AppConfig
      with MarketCache with MarketRepository with MarketData
      with MarketApi with MarketQueryHandler with MarketCommandHandler
      with MarketMeter with MarketTracing,
    Nothing,
    AppEnv
  ] =
    ZLayer.fromFunction(AppEnvLive.apply)
}
