package tarot.application.handlers

import tarot.application.commands.{SpreadCreateCommand, SpreadPublishCommand}
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.photo.{ExternalPhoto, Photo}
import tarot.domain.models.spreads.*
import tarot.layers.AppEnv
import zio.{Clock, ZIO}

import java.time.Instant

final class SpreadPublishCommandHandlerLive extends SpreadPublishCommandHandler {
  def handle(command: SpreadPublishCommand): ZIO[AppEnv, TarotError, Unit] = {
    for {
      tarotRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository)
      spread <- tarotRepository.getSpread(command.spreadId)
        .flatMap(ZIO.fromOption(_).orElseFail(TarotError.NotFound(s"Spread ${command.spreadId} not found")))

      _ <- checkingSpread(spread)
      _ <- publishSpread(spread, command.scheduledAt)
    } yield ()
  }

  private def checkingSpread(spread: Spread) =
    for {
      _ <- ZIO.logInfo(s"Checking spread before publish for ${spread.id}")

      tarotRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository)
      cardCount <- tarotRepository.countCards(spread.id)

      _ <- ZIO.when(spread.spreadStatus != SpreadStatus.Draft) {
        ZIO.fail(TarotError.Conflict(s"Spread $spread.id is not in Draft status"))
      }
      _ <- ZIO.when(cardCount < spread.cardCount) {
        ZIO.fail(TarotError.Conflict(s"Spread $spread.id has only $cardCount out of ${spread.cardCount} cards"))
      }
    } yield ()

  private def publishSpread(spread: Spread, scheduledAt: Instant) =
    for {
      tarotRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository)
      projectConfig <- ZIO.serviceWith[AppEnv](_.appConfig.project)

      now <- Clock.instant
      minTime = now.plus(projectConfig.minFutureTime)
      maxTime = now.plus(projectConfig.maxFutureTime)
      _ <- ZIO.fail(ValidationError(s"scheduledAt must be after $minTime and before $maxTime"))
        .when(scheduledAt.isBefore(minTime) || scheduledAt.isAfter(maxTime))

      _ <- ZIO.logInfo(s"Publishing spread for ${spread.id}")
      spreadStatusUpdate = SpreadStatusUpdate.Ready(spread.id, SpreadStatus.Ready, scheduledAt)
      _ <- tarotRepository.updateSpreadStatus(spreadStatusUpdate)
      _ <- ZIO.logInfo(s"Successfully spread published: ${spread.id}")
    } yield ()
}
