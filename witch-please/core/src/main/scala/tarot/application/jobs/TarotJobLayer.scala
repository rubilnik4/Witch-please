package tarot.application.jobs

import tarot.application.jobs.spreads.{PublishJobLayer, PublishJobLive}
import zio.ZLayer

object TarotJobLayer {
  val live: ZLayer[Any, Nothing, TarotJob] =
    PublishJobLayer.live >>> ZLayer.fromFunction(TarotJobLive.apply)
}
