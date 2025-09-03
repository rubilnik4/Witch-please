package tarot.layers

import tarot.application.commands.TarotCommandHandlerLayer
import tarot.application.queries.TarotQueryHandlerLayer
import tarot.infrastructure.services.TarotServiceLayer
import zio.ZLayer

object TestAppEnvLayer {
  val testAppEnvLive: ZLayer[Any, Throwable, AppEnv] =
    ZLayer.make[AppEnv](
      TestAppConfigLayer.testAppConfigLive,
      TestTarotRepositoryLayer.postgresTarotRepositoryLive,
      TarotServiceLayer.tarotServiceLive,
      TarotCommandHandlerLayer.tarotCommandHandlerLive,
      TarotQueryHandlerLayer.tarotQueryHandlerLive,
      TestTarotMeterLayer.testMarketMeterLive,
      TestTarotTracingLayer.tarotTracingLive,
      AppEnvLayer.appEnvLive
    )
}
