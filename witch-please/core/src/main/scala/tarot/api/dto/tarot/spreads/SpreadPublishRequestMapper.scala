package tarot.api.dto.tarot.spreads

import shared.api.dto.tarot.spreads.*
import shared.infrastructure.services.common.DateTimeService
import tarot.application.commands.spreads.commands.ScheduleSpreadCommand
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.spreads.SpreadId
import tarot.domain.models.users.UserId
import tarot.layers.TarotEnv
import zio.ZIO

object SpreadPublishRequestMapper {
  def fromRequest(request: SpreadPublishRequest, userId: UserId, spreadId: SpreadId): ZIO[TarotEnv, TarotError, ScheduleSpreadCommand] =
    validate(request).as(toCommand(request, userId, spreadId))

  private def toCommand(request: SpreadPublishRequest, userId: UserId, spreadId: SpreadId) =
    ScheduleSpreadCommand(
        userId = userId,
        spreadId = spreadId,
        scheduledAt = request.scheduledAt,
        cardOfDayDelayHours = request.cardOfDayDelayHours
    )
      
  private def validate(request: SpreadPublishRequest): ZIO[TarotEnv, TarotError, Unit] =
    for {
      config <- ZIO.serviceWith[TarotEnv](_.config.publish)
      now <- DateTimeService.getDateTimeNow
      minDateTime = now.minus(config.maxPastTime)
      _ <- ZIO
        .fail(ValidationError(s"scheduledAt must be after $minDateTime"))
        .when(request.scheduledAt.isBefore(minDateTime))
    } yield ()
}
