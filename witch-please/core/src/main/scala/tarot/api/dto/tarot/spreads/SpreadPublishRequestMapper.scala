package tarot.api.dto.tarot.spreads

import shared.api.dto.tarot.spreads.*
import shared.infrastructure.services.common.DateTimeService
import tarot.application.commands.spreads.commands.ScheduleSpreadCommand
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.spreads.SpreadId
import tarot.domain.models.users.UserId
import zio.{IO, ZIO}

object SpreadPublishRequestMapper {
  def fromRequest(request: SpreadPublishRequest, userId: UserId, spreadId: SpreadId): IO[TarotError, ScheduleSpreadCommand] =
    validate(request).as(toCommand(request, userId, spreadId))

  private def toCommand(request: SpreadPublishRequest, userId: UserId, spreadId: SpreadId) =
    ScheduleSpreadCommand(
        userId = userId,
        spreadId = spreadId,
        scheduledAt = request.scheduledAt,
        cardOfDayDelayHours = request.cardOfDayDelayHours
    )
      
  private def validate(request: SpreadPublishRequest): ZIO[Any, TarotError, Unit] =
    for {
      now <- DateTimeService.getDateTimeNow
      _ <- ZIO
        .fail(ValidationError("scheduledAt must be in the future"))
        .when(request.scheduledAt.isBefore(now))
    } yield ()
}