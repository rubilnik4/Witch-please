package tarot.domain.models.spreads

import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

import java.time.Instant
import java.util.UUID

final case class Spread(
    id: UUID,
    title: String,
    cardCount: Integer,
    spreadStatus: SpreadStatus,
    coverPhotoUrl: String,
    time: Instant)
{
  override def toString: String = s"id: $id; title:$title"
}