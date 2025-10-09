package tarot.infrastructure.repositories.spreads

import tarot.domain.models.TarotError
import tarot.domain.models.cards.{Card, CardId}
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.{Spread, SpreadId, SpreadStatusUpdate}
import zio.ZIO


trait SpreadRepository {
  def getSpread(spreadId: SpreadId): ZIO[Any, TarotError, Option[Spread]]
  def getSpreads(projectId: ProjectId): ZIO[Any, TarotError, List[Spread]]
  def existsSpread(spreadId: SpreadId): ZIO[Any, TarotError, Boolean]
  def validateSpread(spreadId: SpreadId): ZIO[Any, TarotError, Boolean]
  def createSpread(spread: Spread): ZIO[Any, TarotError, SpreadId]
  def updateSpreadStatus(spreadStatusUpdate: SpreadStatusUpdate): ZIO[Any, TarotError, Unit]
  def getCards(spreadId: SpreadId): ZIO[Any, TarotError, List[Card]]
  def createCard(card: Card): ZIO[Any, TarotError, CardId]
  def countCards(spreadId: SpreadId): ZIO[Any, TarotError, Long]
}
