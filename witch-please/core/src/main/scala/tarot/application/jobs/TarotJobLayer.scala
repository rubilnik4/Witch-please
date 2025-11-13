package tarot.application.jobs

import tarot.application.jobs.spreads.SpreadJobLayer
import zio.ZLayer

object TarotJobLayer {
  val tarotJobLive: ZLayer[Any, Nothing, TarotJob] =
    SpreadJobLayer.spreadJobLive >>> ZLayer.fromFunction(TarotJobLive.apply)
}
