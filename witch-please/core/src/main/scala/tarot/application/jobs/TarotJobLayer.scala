package tarot.application.jobs

import tarot.application.jobs.publish.{PublishJobLayer, PublishJobLive}
import tarot.layers.TarotEnv
import zio.{ZIO, ZLayer}

object TarotJobLayer {
  val live: ZLayer[Any, Nothing, TarotJob] =
    PublishJobLayer.live >>> ZLayer.fromFunction(TarotJobLive.apply)

  val runner: ZLayer[TarotEnv, Nothing, Unit] =
    ZLayer.scoped {
      for {
        publishJob <- ZIO.serviceWith[TarotEnv](_.jobs.publishJob)
        _ <- publishJob.run.forkScoped
      } yield ()
  }
}
