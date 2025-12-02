package tarot.application.commands.spreads.commands

import tarot.domain.models.authorize.UserId
import tarot.domain.models.photo.PhotoSource
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.SpreadId

import java.time.{Duration, Instant}

final case class ScheduleSpreadCommand(
  spreadId: SpreadId,
  scheduledAt: Instant, 
  cardOfDayDelayHours: Duration
)