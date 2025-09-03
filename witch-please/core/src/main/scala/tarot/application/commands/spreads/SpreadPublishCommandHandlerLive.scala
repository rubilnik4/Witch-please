package tarot.application.commands.spreads

import shared.infrastructure.services.common.DateTimeService
import tarot.application.commands.spreads.SpreadPublishCommand
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.spreads.*
import tarot.layers.AppEnv
import zio.ZIO

import java.time.Instant

final class SpreadPublishCommandHandlerLive extends SpreadPublishCommandHandler {
  def handle(command: SpreadPublishCommand): ZIO[AppEnv, TarotError, Unit] = {
    for {
      spreadRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository.spreadRepository)
      spread <- spreadRepository.getSpread(command.spreadId)
        .flatMap(ZIO.fromOption(_).orElseFail(TarotError.NotFound(s"Spread ${command.spreadId} not found")))

      _ <- checkingSpread(spread)
      _ <- publishSpread(spread, command.scheduledAt)
    } yield ()
  }

  private def checkingSpread(spread: Spread) =
    for {
      _ <- ZIO.logInfo(s"Checking spread before publish for ${spread.id}")

      spreadRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository.spreadRepository)
      cardCount <- spreadRepository.countCards(spread.id)

      _ <- ZIO.when(spread.spreadStatus != SpreadStatus.Draft) {
        ZIO.logError(s"Spread $spread.id is not in Draft status")  *>
          ZIO.fail(TarotError.Conflict(s"Spread $spread.id is not in Draft status"))
      }
      _ <- ZIO.when(cardCount < spread.cardCount) {
        ZIO.logError(s"Spread $spread.id has only $cardCount out of ${spread.cardCount} cards") *>
          ZIO.fail(TarotError.Conflict(s"Spread $spread.id has only $cardCount out of ${spread.cardCount} cards"))
      }
    } yield ()

  private def publishSpread(spread: Spread, scheduledAt: Instant) =
    for {
      spreadRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository.spreadRepository)
      projectConfig <- ZIO.serviceWith[AppEnv](_.appConfig.project)

      now <- DateTimeService.getDateTimeNow
      minTime = now.plus(projectConfig.minFutureTime)
      maxTime = now.plus(projectConfig.maxFutureTime)
      _ <- ZIO.when(scheduledAt.isBefore(minTime) || scheduledAt.isAfter(maxTime)) {
         ZIO.logError(s"scheduledAt must be after $minTime and before $maxTime")
          *> ZIO.fail(ValidationError(s"scheduledAt must be after $minTime and before $maxTime"))
      }
      _ <- ZIO.when(scheduledAt.isBefore(spread.createdAt)) {
        ZIO.logError(s"scheduledAt must be after creation time ${spread.createdAt}")
          *> ZIO.fail(ValidationError(s"scheduledAt must be after creation time ${spread.createdAt}"))
      }

      _ <- ZIO.logInfo(s"Publishing spread for ${spread.id}")
      spreadStatusUpdate = SpreadStatusUpdate.Ready(spread.id, SpreadStatus.Ready, scheduledAt)
      _ <- spreadRepository.updateSpreadStatus(spreadStatusUpdate)
      _ <- ZIO.logInfo(s"Successfully spread published: ${spread.id}")
    } yield ()
}
