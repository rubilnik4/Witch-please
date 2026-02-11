package tarot.infrastructure.repositories.cards

import tarot.domain.models.TarotError
import tarot.domain.models.cards.{Card, CardId, CardUpdate}
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.{Spread, SpreadId, SpreadStatusUpdate}
import zio.ZIO

import java.time.Instant

trait CardRepository {
  def getCard(cardId: CardId): ZIO[Any, TarotError, Option[Card]]
  def getCards(spreadId: SpreadId): ZIO[Any, TarotError, List[Card]]
  def getCardIds(spreadId: SpreadId): ZIO[Any, TarotError, List[CardId]]
  def getCardsCount(spreadId: SpreadId): ZIO[Any, TarotError, Long]
  def existCardPosition(spreadId: SpreadId, position: Int): ZIO[Any, TarotError, Boolean]
  def createCard(card: Card): ZIO[Any, TarotError, CardId]
  def createCards(cards: List[Card]): ZIO[Any, TarotError, List[CardId]]
  def updateCard(cardId: CardId, card: CardUpdate): ZIO[Any, TarotError, Unit]
  def deleteCard(cardId: CardId): ZIO[Any, TarotError, Boolean]
}
