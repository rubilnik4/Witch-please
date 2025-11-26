package tarot.application.commands.spreads

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.UserId
import tarot.domain.models.spreads.{ExternalSpread, Spread, SpreadId}
import tarot.layers.TarotEnv
import zio.ZIO

import java.time.Instant

trait SpreadCommandHandler {
  def createSpread(externalSpread: ExternalSpread, userId: UserId): ZIO[TarotEnv, TarotError, SpreadId]
  def scheduleSpread(spreadId: SpreadId, scheduledAt: Instant, cardOfDayDelayHours: Int): ZIO[TarotEnv, TarotError, Unit]
  def publishPreviewSpread(spread: Spread): ZIO[TarotEnv, TarotError, Unit]
  def publishSpread(spread: Spread, publishAt: Instant): ZIO[TarotEnv, TarotError, Unit]
  def deleteSpread(spreadId: SpreadId): ZIO[TarotEnv, TarotError, Unit]
}

