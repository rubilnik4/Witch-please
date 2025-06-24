package tarot.layers

import tarot.application.handlers.TarotCommandHandlerLayer
import tarot.infrastructure.services.TarotServiceLayer
import tarot.infrastructure.services.photo.PhotoServiceLayer
import zio.ZLayer

object TestAppEnvLayer {
  val testAppEnvLive: ZLayer[Any, Throwable, AppEnv] =
    ZLayer.make[AppEnv](
      TestAppConfigLayer.testAppConfigLive,
      TestTarotRepositoryLayer.postgresTarotRepositoryLive,
      TarotServiceLayer.tarotServiceLive,
      TarotCommandHandlerLayer.tarotCommandHandlerLive,
      TestTarotMeterLayer.testMarketMeterLive,
      TestTarotTracingLayer.tarotTracingLive,
      AppEnvLayer.appEnvLive
    )
}
