package tarot.application.queries.cardsOfDay

import tarot.infrastructure.repositories.cardOfDay.CardOfDayRepository
import tarot.infrastructure.repositories.cards.CardRepository
import zio.ZLayer

object CardOfDayQueryHandlerLayer {
  val live: ZLayer[CardOfDayRepository, Nothing, CardOfDayQueryHandler] =
    ZLayer.fromFunction(CardOfDayQueryHandlerLive(_))
}
