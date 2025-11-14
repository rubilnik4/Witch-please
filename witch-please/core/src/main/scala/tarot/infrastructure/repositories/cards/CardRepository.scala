package tarot.infrastructure.repositories.cards

import tarot.domain.models.TarotError
import tarot.domain.models.cards.{Card, CardId}
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.{Spread, SpreadId, SpreadStatusUpdate}
import zio.ZIO

import java.time.Instant


trait CardRepository {
  def getCards(spreadId: SpreadId): ZIO[Any, TarotError, List[Card]]
  def getCardsCount(spreadId: SpreadId): ZIO[Any, TarotError, Long]
  def createCard(card: Card): ZIO[Any, TarotError, CardId]
}
