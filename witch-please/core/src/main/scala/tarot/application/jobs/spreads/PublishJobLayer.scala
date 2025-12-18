package tarot.application.jobs.spreads

import tarot.layers.TarotEnv
import zio.{ZIO, ZLayer}

object PublishJobLayer {
  val live: ZLayer[Any, Nothing, PublishJob] =
    ZLayer.succeed(new PublishJobLive)

  val runner: ZLayer[TarotEnv, Nothing, Unit] =
    ZLayer.scoped {
      for {
        publishJob <- ZIO.serviceWith[TarotEnv](_.jobs.publishJob)
        _ <- publishJob.run.forkScoped
      } yield ()
    }
}
