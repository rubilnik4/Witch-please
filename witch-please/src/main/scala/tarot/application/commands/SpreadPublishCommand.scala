package tarot.application.commands

import tarot.domain.models.spreads.{ExternalSpread, Spread, SpreadId}

import java.time.Instant

case class SpreadPublishCommand(spreadId: SpreadId, scheduledAt: Instant)
