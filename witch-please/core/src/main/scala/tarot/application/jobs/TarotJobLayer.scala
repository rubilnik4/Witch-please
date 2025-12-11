package tarot.application.jobs

import tarot.application.jobs.spreads.SpreadJobLayer
import zio.ZLayer

object TarotJobLayer {
  val live: ZLayer[Any, Nothing, TarotJob] =
    SpreadJobLayer.live >>> ZLayer.fromFunction(TarotJobLive.apply)
}
