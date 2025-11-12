package tarot.infrastructure.jobs

import tarot.infrastructure.jobs.spreads.*
import zio.ZLayer

object TarotJobLayer {
  val tarotJobLive: ZLayer[Any, Nothing, TarotJob] =
    SpreadJobLayer.spreadJobLive >>> ZLayer.fromFunction(TarotJobLive.apply)
}
