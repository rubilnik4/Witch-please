package tarot.application.commands.spreads

import tarot.application.commands.spreads.commands.*
import tarot.domain.models.TarotError
import tarot.domain.models.authorize.UserId
import tarot.domain.models.spreads.{Spread, SpreadId}
import tarot.layers.TarotEnv
import zio.ZIO

import java.time.*

trait SpreadCommandHandler {
  def createSpread(command: CreateSpreadCommand): ZIO[TarotEnv, TarotError, SpreadId]
  def updateSpread(command: UpdateSpreadCommand): ZIO[TarotEnv, TarotError, Unit]
  def scheduleSpread(command: ScheduleSpreadCommand): ZIO[TarotEnv, TarotError, Unit]
  def publishSpread(spreadId: SpreadId, publishAt: Instant): ZIO[TarotEnv, TarotError, Unit]
  def deleteSpread(spreadId: SpreadId): ZIO[TarotEnv, TarotError, Unit]
}

