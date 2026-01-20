package tarot.application.queries.cardsOfDay

import tarot.domain.models.TarotError
import tarot.domain.models.cards.CardId
import tarot.domain.models.cardsOfDay.{CardOfDay, CardOfDayId}
import tarot.domain.models.spreads.SpreadId
import tarot.layers.TarotEnv
import zio.ZIO

import java.time.Instant

trait CardOfDayQueryHandler {
  def getCardOfDay(cardOfDayId: CardOfDayId): ZIO[TarotEnv, TarotError, CardOfDay]
  def getCardOfDayBySpread(spreadId: SpreadId): ZIO[TarotEnv, TarotError, CardOfDay]
  def getCardOfDayBySpreadOption(spreadId: SpreadId): ZIO[TarotEnv, TarotError, Option[CardOfDay]]
  def getCardOfDayByCard(cardId: CardId): ZIO[TarotEnv, TarotError, CardOfDay]
  def getCardOfDayByCardOption(cardId: CardId): ZIO[TarotEnv, TarotError, Option[CardOfDay]]
  def getScheduledCardsOfDay(deadline: Instant, limit: Int): ZIO[TarotEnv, TarotError, List[CardOfDay]]
}

