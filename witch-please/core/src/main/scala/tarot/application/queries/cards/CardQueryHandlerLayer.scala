package tarot.application.queries.cards

import tarot.infrastructure.repositories.cards.CardRepository
import zio.ZLayer

object CardQueryHandlerLayer {
  val live: ZLayer[CardRepository, Nothing, CardQueryHandler] =
    ZLayer.fromFunction(new CardQueryHandlerLive(_))
}
