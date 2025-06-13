package tarot.api.dto

import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.spreads.{Spread, SpreadStatus}
import zio.{IO, ZIO}
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

import java.util.UUID

final case class SpreadRequest(title: String, cardCount: Int, coverPhotoId: String)

object SpreadResponse {
  given JsonCodec[SpreadRequest] = DeriveJsonCodec.gen
  given Schema[SpreadRequest] = DeriveSchema.gen
}

object SpreadMapper {
  def toDomain(request: SpreadRequest): Spread =
    Spread(
      id = UUID.randomUUID(),
      title = request.title,
      cardCount = request.cardCount,
      spreadStatus = SpreadStatus.Draft,
      coverPhotoId = request.coverPhotoId
    )
}

object SpreadRequest {
  def validate(req: SpreadRequest): IO[TarotError, SpreadRequest] = {
    for {
      _ <- ZIO.fail(ValidationError("title must not be empty")).when(req.title.trim.isEmpty)
      _ <- ZIO.fail(ValidationError("cardCount must be > 0")).when(req.cardCount <= 0)
      _ <- ZIO.fail(ValidationError("coverPhotoId must not be empty")).when(req.coverPhotoId.trim.isEmpty)
    } yield req
  }
}