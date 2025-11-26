package tarot.application.jobs.spreads

import shared.infrastructure.services.common.DateTimeService
import tarot.application.jobs.spreads.SpreadPublishType.PreviewPublished
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
      queryHandler <- ZIO.serviceWith[TarotEnv](_.tarotQueryHandler.spreadQueryHandler)
      commandHandler <- ZIO.serviceWith[TarotEnv](_.tarotCommandHandler.spreadCommandHandler)

      now <- DateTimeService.getDateTimeNow
      deadline = now.plus(config.lookAhead)

      scheduledSpreads <- queryHandler.getScheduledSpreads(deadline, config.batchLimit)
      scheduledResults <- ZIO.foreach(scheduledSpreads) { spread =>
        commandHandler.publishPreviewSpread(spread)
          .either.map(SpreadPublishResult(spread.id, SpreadPublishType.PreviewPublished, _))
      }

      previewSpreads <- queryHandler.getPreviewSpreads(deadline, config.batchLimit)
      previewResults <- ZIO.foreach(previewSpreads) { spread =>
        commandHandler.publishSpread(spread, now)
          .either.map(SpreadPublishResult(spread.id, SpreadPublishType.Published, _))
      }

      publishResults = scheduledResults ++ previewResults
      _ <- logPublishSpread(publishResults)
    } yield publishResults

  private def logPublishSpread(publishResults: List[SpreadPublishResult]) =
    val successCount = publishResults.count(_.result.isRight)
    val previewPublished = publishResults.count(result => result.result.isRight & result.publishType == SpreadPublishType.PreviewPublished)
    val published = publishResults.count(result => result.result.isRight & result.publishType == SpreadPublishType.Published)
    val failed = publishResults.collect { case result if result.result.isLeft => result }
    for {
      _ <- ZIO.when(successCount > 0) {
        ZIO.logInfo(s"Spread job: published=$successCount, previewPublished=$previewPublished, card of day published=$published")
      }
      _ <- ZIO.when(failed.nonEmpty) {
        val details = failed.map(result => s"${result.id}: ${result.result.left}").mkString("\n  ")
        ZIO.logError(s"Spread job: failed=${failed.size}\n  $details")
      }
    } yield ()
}
