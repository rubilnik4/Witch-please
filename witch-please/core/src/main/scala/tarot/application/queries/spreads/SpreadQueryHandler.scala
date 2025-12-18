package tarot.application.queries.spreads

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.UserId
import tarot.domain.models.spreads.{Spread, SpreadId}
import tarot.layers.TarotEnv
import zio.ZIO

import java.time.Instant

trait SpreadQueryHandler {
  def getSpread(spreadId: SpreadId): ZIO[TarotEnv, TarotError, Spread]
  def getSpreads(userId: UserId): ZIO[TarotEnv, TarotError, List[Spread]]
  def getScheduledSpreads(deadline: Instant, limit: Int): ZIO[TarotEnv, TarotError, List[Spread]]
}
