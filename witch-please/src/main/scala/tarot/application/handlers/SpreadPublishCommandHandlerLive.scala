package tarot.application.handlers

import tarot.application.commands.{SpreadCreateCommand, SpreadPublishCommand}
import tarot.domain.models.TarotError
import tarot.domain.models.contracts.SpreadId
import tarot.domain.models.photo.{ExternalPhoto, Photo}
import tarot.domain.models.spreads.*
import tarot.layers.AppEnv
import zio.ZIO

import java.time.Instant

final class SpreadPublishCommandHandlerLive extends SpreadPublishCommandHandler {
  def handle(command: SpreadPublishCommand): ZIO[AppEnv, TarotError, Unit] = {
    for {
      _ <- checkingSpread(command.spreadId)
      _ <- publishSpread(command.spreadId)
    } yield ()
  }

  private def checkingSpread(spreadId: SpreadId) =
    for {
      _ <- ZIO.logInfo(s"Checking spread before publish for ${spreadId}")

      tarotRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository)
      spread <- tarotRepository.getSpread(spreadId)
        .flatMap(ZIO.fromOption(_).orElseFail(TarotError.NotFound(s"Spread $spreadId not found")))

      cardCount <- tarotRepository.countCards(spreadId)

      _ <- ZIO.when(spread.spreadStatus != SpreadStatus.Draft) {
        ZIO.fail(TarotError.Conflict(s"Spread $spreadId is not in Draft status"))
      }
      _ <- ZIO.when(cardCount < spread.cardCount) {
        ZIO.fail(TarotError.Conflict(s"Spread $spreadId has only $cardCount out of ${spread.cardCount} cards"))
      }
    } yield ()

  private def publishSpread(spreadId: SpreadId) =
    for {
      tarotRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository)

      _ <- ZIO.logInfo(s"Publishing spread for $spreadId")
      _ <- tarotRepository.updateSpreadStatus(spreadId, SpreadStatus.Published)
      _ <- ZIO.logInfo(s"Successfully spread published: $spreadId")
    } yield ()
}
