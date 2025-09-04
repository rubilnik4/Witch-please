package tarot.layers

import tarot.application.commands.TarotCommandHandlerLayer
import tarot.application.queries.TarotQueryHandlerLayer
import tarot.infrastructure.services.TarotServiceLayer
import zio.ZLayer

object TestAppEnvLayer {
  val testAppEnvLive: ZLayer[Any, Throwable, TarotEnv] =
    ZLayer.make[TarotEnv](
      TestAppConfigLayer.testAppConfigLive,
      TestTarotRepositoryLayer.postgresTarotRepositoryLive,
      TarotServiceLayer.tarotServiceLive,
      TarotCommandHandlerLayer.tarotCommandHandlerLive,
      TarotQueryHandlerLayer.tarotQueryHandlerLive,
      TestTarotMeterLayer.testMarketMeterLive,
      TestTarotTracingLayer.tarotTracingLive,
      TarotEnvLayer.envLive
    )
}
