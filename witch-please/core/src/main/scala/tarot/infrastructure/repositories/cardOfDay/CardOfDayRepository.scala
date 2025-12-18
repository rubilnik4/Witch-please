package tarot.infrastructure.repositories.cardOfDay

import tarot.domain.models.TarotError
import tarot.domain.models.cardOfDay.{CardOfDay, CardOfDayId, CardOfDayStatusUpdate}
import tarot.domain.models.cards.{Card, CardId, CardUpdate}
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.{Spread, SpreadId, SpreadStatusUpdate}
import zio.ZIO

import java.time.Instant

trait CardOfDayRepository {
  def getCardOfDay(spreadId: SpreadId): ZIO[Any, TarotError, Option[CardOfDay]]
  def getScheduledCardsOfDay(deadline: Instant, limit: Int): ZIO[Any, TarotError, List[CardOfDay]]
  def existCardOfDay(spreadId: SpreadId): ZIO[Any, TarotError, Boolean]
  def createCardOfDay(cardOfDay: CardOfDay): ZIO[Any, TarotError, CardOfDayId]
  def updateCardOfDayStatus(cardOfDayStatusUpdate: CardOfDayStatusUpdate): ZIO[Any, TarotError, Unit]
}
