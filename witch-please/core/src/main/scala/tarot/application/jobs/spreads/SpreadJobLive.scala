package tarot.application.jobs.spreads

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
      _ <- publishSpreads()

      config <- ZIO.serviceWith[TarotEnv](_.config.publish)
      _ <- ZIO.sleep(config.tick)
      _ <- loop
    } yield ()

  def publishSpreads(): ZIO[TarotEnv, TarotError, List[SpreadPublishResult]] =
    for {
      config <- ZIO.serviceWith[TarotEnv](_.config.publish)
      queryHandler <- ZIO.serviceWith[TarotEnv](_.tarotQueryHandler.spreadsQueryHandler)
      commandHandler <- ZIO.serviceWith[TarotEnv](_.tarotCommandHandler.spreadCommandHandler)

      now <- DateTimeService.getDateTimeNow
      deadline = now.plus(config.lookAhead)
      spreads <- queryHandler.getScheduleSpreads(deadline, config.batchLimit)

      publishResults <- ZIO.foreach(spreads) { spread =>
        commandHandler.publishSpread(spread, now).either.map(SpreadPublishResult(spread.id, _))
      }
      _ <- logPublishSpread(publishResults)
    } yield publishResults

  private def logPublishSpread(publishResults: List[SpreadPublishResult]) =
    val successCount = publishResults.count(_.result.isRight)
    val failed = publishResults.collect { case result if result.result.isLeft => result }
    for {
      _ <- ZIO.when(successCount > 0) {
        ZIO.logInfo(s"Spread job: published=$successCount")
      }
      _ <- ZIO.when(failed.nonEmpty) {
        val details = failed.map(result => s"${result.id}: ${result.result.left}").mkString("\n  ")
        ZIO.logError(s"Spread job: failed=${failed.size}\n  $details")
      }
    } yield ()
}
