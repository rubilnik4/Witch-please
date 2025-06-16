package tarot.domain.models.spreads

import tarot.domain.models.photo.{PhotoFile, PhotoLocation}
import tarot.infrastructure.services.common.DateTimeService
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

import java.time.Instant
import java.util.UUID

final case class Spread(
    id: UUID,
    title: String,
    cardCount: Integer,
    spreadStatus: SpreadStatus,
    coverPhoto: PhotoLocation,
    time: Instant)
{
  override def toString: String = s"id: $id; title:$title"
}

object SpreadMapper {
  def fromExternal(externalSpread: ExternalSpread, coverPhoto: PhotoLocation): Spread =
    Spread(
      id = UUID.randomUUID(),
      title = externalSpread.title,
      cardCount = externalSpread.cardCount,
      spreadStatus = SpreadStatus.Draft,
      coverPhoto = coverPhoto,
      time = DateTimeService.getDateTimeNow
    )
}