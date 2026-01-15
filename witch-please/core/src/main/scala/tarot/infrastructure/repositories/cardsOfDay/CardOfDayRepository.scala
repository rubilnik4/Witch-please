package tarot.infrastructure.repositories.cardsOfDay

import tarot.domain.models.TarotError
import tarot.domain.models.cardsOfDay.*
import tarot.domain.models.spreads.SpreadId
import zio.ZIO

import java.time.Instant

trait CardOfDayRepository {
  def getCardOfDay(cardOfDayId: CardOfDayId): ZIO[Any, TarotError, Option[CardOfDay]]
  def getCardOfDayBySpread(spreadId: SpreadId): ZIO[Any, TarotError, Option[CardOfDay]]
  def getScheduledCardsOfDay(deadline: Instant, limit: Int): ZIO[Any, TarotError, List[CardOfDay]]
  def existCardOfDay(spreadId: SpreadId): ZIO[Any, TarotError, Boolean]
  def createCardOfDay(cardOfDay: CardOfDay): ZIO[Any, TarotError, CardOfDayId]
  def updateCardOfDay(cardOfDayId: CardOfDayId, cardOfDayUpdate: CardOfDayUpdate): ZIO[Any, TarotError, Unit]
  def updateCardOfDayStatus(cardOfDayStatusUpdate: CardOfDayStatusUpdate): ZIO[Any, TarotError, Unit]
  def deleteCardOfDay(cardOfDayId: CardOfDayId): ZIO[Any, TarotError, Boolean]
}
