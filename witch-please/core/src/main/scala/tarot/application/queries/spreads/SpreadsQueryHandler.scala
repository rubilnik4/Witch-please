package tarot.application.queries.spreads

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.User
import tarot.domain.models.projects.{Project, ProjectId}
import tarot.domain.models.spreads.{Spread, SpreadId}
import tarot.layers.TarotEnv
import zio.ZIO

import java.time.Instant

trait SpreadsQueryHandler {
  def getSpread(spreadId: SpreadId): ZIO[TarotEnv, TarotError, Spread]
  def getSpreads(projectId: ProjectId): ZIO[TarotEnv, TarotError, List[Spread]]
  def getScheduleSpreads(deadline: Instant, from: Option[Instant], limit: Int): ZIO[TarotEnv, TarotError, List[Spread]]
}
