package tarot.application.commands

import tarot.domain.models.spreads.SpreadId

import java.time.Instant

case class SpreadPublishCommand(spreadId: SpreadId, scheduledAt: Instant)
