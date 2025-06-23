package tarot.layers

import tarot.application.handlers.TarotCommandHandlerLayer
import tarot.infrastructure.services.photo.TarotPhotoServiceLayer
import zio.ZLayer

object TestAppEnvLayer {
  val testAppEnvLive: ZLayer[Any, Throwable, AppEnv] =
    ZLayer.make[AppEnv](
      TestAppConfigLayer.testAppConfigLive,
      TestTarotRepositoryLayer.postgresTarotRepositoryLive,
      TarotPhotoServiceLayer.tarotPhotoServiceLive,
      TarotCommandHandlerLayer.tarotCommandHandlerLive,
      TestTarotMeterLayer.testMarketMeterLive,
      TestTarotTracingLayer.tarotTracingLive,
      AppEnvLayer.appEnvLive
    )
}
