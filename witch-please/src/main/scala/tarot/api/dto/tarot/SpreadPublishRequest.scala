package tarot.api.dto.tarot

import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import zio.{Clock, ZIO}
import zio.json.*
import zio.schema.*

import java.time.Instant

final case class SpreadPublishRequest(
  scheduledAt: Instant
) derives JsonCodec, Schema

object SpreadPublishRequest {
  def validate(request: SpreadPublishRequest): ZIO[Any, TarotError, Unit] =
    for {
      now <- Clock.instant
      _ <- ZIO
        .fail(ValidationError("scheduledAt must be in the future"))
        .when(request.scheduledAt.isBefore(now))
    } yield ()
}