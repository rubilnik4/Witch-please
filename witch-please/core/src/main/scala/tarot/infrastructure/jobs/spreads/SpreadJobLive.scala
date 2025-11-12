package tarot.infrastructure.jobs.spreads

import shared.infrastructure.services.common.DateTimeService
import tarot.domain.models.TarotError
import tarot.layers.TarotEnv
import zio.ZIO

final class SpreadJobLive extends SpreadJob {
  override def run: ZIO[TarotEnv, Nothing, Unit] =
    ZIO.logInfo("Spread publisher started") *>
      loop.ignore

  private def loop: ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- publishSpread()

      config <- ZIO.serviceWith[TarotEnv](_.config.publish)
      _ <- ZIO.sleep(config.tick)
      _ <- loop
    } yield ()

  private def publishSpread() =
    for {
      config <- ZIO.serviceWith[TarotEnv](_.config.publish)
      queryHandler <- ZIO.serviceWith[TarotEnv](_.tarotQueryHandler.spreadsQueryHandler)
      commandHandler <- ZIO.serviceWith[TarotEnv](_.tarotCommandHandler.spreadCommandHandler)

      now <- DateTimeService.getDateTimeNow
      deadline = now.plus(config.lookAhead)
      spreads <- queryHandler.getScheduleSpreads(deadline, None, config.batchLimit)

      _ <- ZIO.foreachDiscard(spreads) { spread =>
        commandHandler.publishSpread(spread, now)
          .catchAll(error => ZIO.logError(s"Spread publishing ${spread.id} failed: $error"))
      }
    } yield ()
}
