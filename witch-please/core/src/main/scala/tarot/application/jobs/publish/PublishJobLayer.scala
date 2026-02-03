package tarot.application.jobs.publish

import tarot.layers.TarotEnv
import zio.{ZIO, ZLayer}

object PublishJobLayer {
  val live: ZLayer[Any, Nothing, PublishJob] =
    ZLayer.succeed(new PublishJobLive)
}
