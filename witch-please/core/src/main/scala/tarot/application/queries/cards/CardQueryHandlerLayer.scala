package tarot.application.queries.cards

import tarot.infrastructure.repositories.spreads.SpreadRepository
import zio.ZLayer

object CardQueryHandlerLayer {
  val live: ZLayer[SpreadRepository, Nothing, CardQueryHandler] =
    ZLayer.fromFunction(new CardQueryHandlerLive(_))
}
