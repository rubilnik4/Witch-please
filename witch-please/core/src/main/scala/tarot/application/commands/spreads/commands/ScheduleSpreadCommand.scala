package tarot.application.commands.spreads.commands

import tarot.domain.models.photo.PhotoSource
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.SpreadId
import tarot.domain.models.users.UserId

import java.time.{Duration, Instant}

final case class ScheduleSpreadCommand(
  spreadId: SpreadId,
  scheduledAt: Instant, 
  cardOfDayDelayHours: Duration
)