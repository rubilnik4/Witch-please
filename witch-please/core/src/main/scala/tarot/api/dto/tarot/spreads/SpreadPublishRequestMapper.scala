package tarot.api.dto.tarot.spreads

import shared.api.dto.tarot.spreads.*
import shared.infrastructure.services.common.DateTimeService
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import zio.json.*
import zio.schema.*
import zio.{Clock, ZIO}

import java.time.Instant

object SpreadPublishRequestMapper {
  def validate(request: SpreadPublishRequest): ZIO[Any, TarotError, Unit] =
    for {
      now <- DateTimeService.getDateTimeNow
      _ <- ZIO
        .fail(ValidationError("scheduledAt must be in the future"))
        .when(request.scheduledAt.isBefore(now))
    } yield ()
}