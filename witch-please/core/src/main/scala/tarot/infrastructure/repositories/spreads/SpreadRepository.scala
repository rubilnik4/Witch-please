package tarot.infrastructure.repositories.spreads

import tarot.domain.models.TarotError
import tarot.domain.models.cards.{Card, CardId}
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.{Spread, SpreadId, SpreadStatusUpdate, SpreadUpdate}
import zio.ZIO

import java.time.Instant

trait SpreadRepository {
  def getSpread(spreadId: SpreadId): ZIO[Any, TarotError, Option[Spread]]
  def getSpreads(projectId: ProjectId): ZIO[Any, TarotError, List[Spread]]
  def getScheduledSpreads(deadline: Instant, limit: Int): ZIO[Any, TarotError, List[Spread]]
  def existsSpread(spreadId: SpreadId): ZIO[Any, TarotError, Boolean]
  def createSpread(spread: Spread): ZIO[Any, TarotError, SpreadId]
  def updateSpreadStatus(spreadStatusUpdate: SpreadStatusUpdate): ZIO[Any, TarotError, Unit]
  def updateSpread(spreadId: SpreadId, spread: SpreadUpdate): ZIO[Any, TarotError, Unit]
  def deleteSpread(spreadId: SpreadId): ZIO[Any, TarotError, Boolean]
}
