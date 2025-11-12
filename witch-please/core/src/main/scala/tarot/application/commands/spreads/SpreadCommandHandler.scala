package tarot.application.commands.spreads

import tarot.domain.models.TarotError
import tarot.domain.models.spreads.{ExternalSpread, SpreadId}
import tarot.layers.TarotEnv
import zio.ZIO

import java.time.Instant

trait SpreadCommandHandler {
  def createSpread(externalSpread: ExternalSpread): ZIO[TarotEnv, TarotError, SpreadId]
  def scheduleSpread(spreadId: SpreadId, scheduledAt: Instant): ZIO[TarotEnv, TarotError, Unit]
  def deleteSpread(spreadId: SpreadId): ZIO[TarotEnv, TarotError, Unit]
}

