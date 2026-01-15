package tarot.application.commands.spreads

import tarot.infrastructure.repositories.spreads.SpreadRepository
import zio.ZLayer

object SpreadCommandHandlerLayer {
  val live: ZLayer[SpreadRepository, Nothing, SpreadCommandHandler] =
    ZLayer.fromFunction(new SpreadCommandHandlerLive(_))
}
