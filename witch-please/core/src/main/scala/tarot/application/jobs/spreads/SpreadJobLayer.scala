package tarot.application.jobs.spreads

import tarot.layers.TarotEnv
import zio.{ZIO, ZLayer}

object SpreadJobLayer {
  val live: ZLayer[Any, Nothing, SpreadJob] =
    ZLayer.succeed(new SpreadJobLive)

  val runner: ZLayer[TarotEnv, Nothing, Unit] =
    ZLayer.scoped {
      for {
        spreadJob <- ZIO.serviceWith[TarotEnv](_.jobs.spreadJob)
        _ <- spreadJob.run.forkScoped
      } yield ()
    }
}
