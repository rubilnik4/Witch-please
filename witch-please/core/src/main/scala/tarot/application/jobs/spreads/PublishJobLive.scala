package tarot.application.jobs.spreads

import shared.infrastructure.services.common.DateTimeService
import tarot.domain.models.TarotError
import tarot.layers.TarotEnv
import zio.ZIO

final class PublishJobLive extends PublishJob {
  override def run: ZIO[TarotEnv, Nothing, Unit] =
    ZIO.logInfo("Spread publisher started") *>
      loop.ignore

  private def loop: ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- publish()

      config <- ZIO.serviceWith[TarotEnv](_.config.publish)
      _ <- ZIO.sleep(config.tick)
      _ <- loop
    } yield ()

  def publish(): ZIO[TarotEnv, TarotError, List[PublishJobResult]] =
    for {
      config <- ZIO.serviceWith[TarotEnv](_.config.publish)
      spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
      cardOfDayQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardOfDayQueryHandler)
      spreadCommandHandler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.spreadCommandHandler)
      cardOfDayCommandHandler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.cardOfDayCommandHandler)

      now <- DateTimeService.getDateTimeNow
      deadline = now.plus(config.lookAhead)

      spreads <- spreadQueryHandler.getScheduledSpreads(deadline, config.batchLimit)
      spreadResults <- ZIO.foreach(spreads) { spread =>
        spreadCommandHandler.publishSpread(spread.id, now).either.map(PublishJobResult.Spread(spread.id, _))
      }

      cardsOfDay <- cardOfDayQueryHandler.getScheduledCardsOfDay(deadline, config.batchLimit)
      cardOfDayResults <- ZIO.foreach(cardsOfDay) { cardOfDay =>
        cardOfDayCommandHandler.publishCardOfDay(cardOfDay.id, now).either.map(PublishJobResult.CardOfDay(cardOfDay.id, _))
      }

      publishResults = spreadResults ++ cardOfDayResults
      _ <- logPublishSpread(publishResults)
    } yield publishResults

  private def logPublishSpread(publishResults: List[PublishJobResult]) =
    val successCount = publishResults.count(_.result.isRight)
    val spreadsPublished = publishResults.count {
      case PublishJobResult.Spread(_, result) => result.isRight
      case _ => false
    }
    val cardsOfDayPublished = publishResults.count {
      case PublishJobResult.CardOfDay(_, result) => result.isRight
      case _ => false
    }
    val failed = publishResults.collect { case result if result.result.isLeft => result }
    for {
      _ <- ZIO.when(successCount > 0) {
        ZIO.logInfo(s"Spread job: published=$successCount, spreads=$spreadsPublished, card of day=$cardsOfDayPublished")
      }
      _ <- ZIO.when(failed.nonEmpty) {        
        val details = failed.collect {
          case PublishJobResult.Spread(id, Left(error)) => s"Spread $id: $error"
          case PublishJobResult.CardOfDay(id, Left(error)) => s"CardOfDay $id: $error"
        }.mkString("\n  ")
        ZIO.logError(s"Spread job: failed=${failed.size}\n  $details")
      }
    } yield ()
}
