package tarot.application.handlers

import tarot.application.commands.{SpreadCreateCommand, SpreadPublishCommand}
import tarot.domain.models.TarotError
import tarot.domain.models.contracts.SpreadId
import tarot.domain.models.photo.{ExternalPhoto, Photo}
import tarot.domain.models.spreads.{ExternalSpread, Spread, SpreadMapper}
import tarot.layers.AppEnv
import zio.ZIO

import java.time.Instant

final class SpreadPublishCommandHandlerLive extends SpreadPublishCommandHandler {
  def handle(command: SpreadPublishCommand): ZIO[AppEnv, TarotError, Unit] = {
    for {
      _ <- ZIO.logInfo(s"Executing publish spread command for ${command.spreadId}")

      tarotRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository)
      spreadId <- tarotRepository.validateSpread(command.spreadId)

      _ <- ZIO.logInfo(s"Successfully spread published: $command.spreadId")
    } yield ()
  }
}
