package tarot.infrastructure.repositories.cards

import tarot.domain.models.TarotError
import tarot.domain.models.cards.{Card, CardId}
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.{Spread, SpreadId, SpreadStatusUpdate}
import zio.ZIO

import java.time.Instant

trait CardRepository {
  def getCards(spreadId: SpreadId): ZIO[Any, TarotError, List[Card]]
  def getCardIds(spreadId: SpreadId): ZIO[Any, TarotError, List[CardId]]
  def getCardsCount(spreadId: SpreadId): ZIO[Any, TarotError, Long]
  def existCardPosition(spreadId: SpreadId, position: Int): ZIO[Any, TarotError, Boolean]
  def createCard(card: Card): ZIO[Any, TarotError, CardId]
  def deleteCard(cardId: CardId): ZIO[Any, TarotError, Boolean]
}
