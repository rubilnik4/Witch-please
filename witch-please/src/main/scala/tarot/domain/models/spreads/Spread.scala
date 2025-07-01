package tarot.domain.models.spreads

import tarot.domain.models.photo.{PhotoFile, PhotoOwnerType, PhotoSource, StoredPhotoSource}
import tarot.infrastructure.services.common.DateTimeService
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

import java.time.Instant
import java.util.UUID

final case class Spread(
    id: UUID,
    title: String,
    cardCount: Int,
    spreadStatus: SpreadStatus,
    coverPhoto: PhotoSource,
    time: Instant)
{
  override def toString: String = s"spread id: $id; title:$title"
}

object SpreadMapper {
  def fromExternal(externalSpread: ExternalSpread, storedPhoto: StoredPhotoSource): Spread = {
    val id = UUID.randomUUID()
    Spread(
      id = id,
      title = externalSpread.title,
      cardCount = externalSpread.cardCount,
      spreadStatus = SpreadStatus.Draft,
      coverPhoto = PhotoSource.toPhotoSource(storedPhoto, PhotoOwnerType.Spread, id),
      time = DateTimeService.getDateTimeNow
    )
  }
}