package tarot.infrastructure.jobs.spreads

import shared.infrastructure.services.common.DateTimeService
import tarot.domain.models.TarotError
import tarot.domain.models.spreads.{Spread, SpreadStatusUpdate}
import tarot.layers.TarotEnv
import zio.ZIO

final class SpreadJobLive extends SpreadJob {
  override def run: ZIO[TarotEnv, Nothing, Unit] =
    ZIO.logInfo("Spread publisher started") *>
      loop.ignore

  private def loop: ZIO[TarotEnv, TarotError, Unit] =
    for {
      config <- ZIO.serviceWith[TarotEnv](_.config.publish)
      repository <- ZIO.serviceWith[TarotEnv](_.tarotRepository.spreadRepository)

      now <- DateTimeService.getDateTimeNow
      deadline = now.plus(config.lookAhead)
      spreads <- repository.getReadySpreads(deadline, None, config.batchLimit)

      _ <- ZIO.foreachDiscard(spreads) { spread =>
        publishSpread(spread)
          .catchAll(error => ZIO.logError(s"Spread publishing ${spread.id} failed: $error"))
      }

      _ <- ZIO.sleep(config.tick)
      _ <- loop
    } yield ()

  private def publishSpread(spread: Spread): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Publishing spread ${spread.id} ")

      repository <- ZIO.serviceWith[TarotEnv](_.tarotRepository.spreadRepository)

      now <- DateTimeService.getDateTimeNow
      spreadStatusUpdate = SpreadStatusUpdate.Published(spread.id, now)
      _ <- repository.updateSpreadStatus(spreadStatusUpdate)
    } yield ()
}
