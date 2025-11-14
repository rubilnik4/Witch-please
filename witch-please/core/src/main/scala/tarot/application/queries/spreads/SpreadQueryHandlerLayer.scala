package tarot.application.queries.spreads

import tarot.infrastructure.repositories.spreads.SpreadRepository
import zio.ZLayer

object SpreadQueryHandlerLayer {
  val live: ZLayer[SpreadRepository, Nothing, SpreadQueryHandler] =
    ZLayer.fromFunction(new SpreadsQueryHandlerLive(_))
}
