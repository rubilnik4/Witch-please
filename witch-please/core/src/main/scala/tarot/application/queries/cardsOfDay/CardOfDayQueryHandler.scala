package tarot.application.queries.cardsOfDay

import tarot.domain.models.TarotError
import tarot.domain.models.cardOfDay.CardOfDay
import tarot.domain.models.cards.*
import tarot.domain.models.spreads.{Spread, SpreadId}
import tarot.layers.TarotEnv
import zio.ZIO

import java.time.Instant

trait CardOfDayQueryHandler {
  def getCardOfDay(spreadId: SpreadId): ZIO[TarotEnv, TarotError, CardOfDay]
  def getScheduledCardsOfDay(deadline: Instant, limit: Int): ZIO[TarotEnv, TarotError, List[CardOfDay]]
}
