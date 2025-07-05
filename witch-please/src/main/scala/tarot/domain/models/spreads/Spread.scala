package tarot.domain.models.spreads

import tarot.domain.models.photo.{PhotoFile, PhotoOwnerType, Photo, PhotoSource}
import tarot.infrastructure.services.common.DateTimeService
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

import java.time.Instant
import java.util.UUID

final case class Spread(
  id: SpreadId,
  title: String,
  cardCount: Int,
  spreadStatus: SpreadStatus,
  coverPhoto: Photo,
  createdAt: Instant,
  scheduledAt: Option[Instant],
  publishedAt: Option[Instant]
)
{
  override def toString: String = s"spread id: $id; title:$title"
}

object SpreadMapper {
  def fromExternal(externalSpread: ExternalSpread, storedPhoto: PhotoSource): Spread = {
    val id = UUID.randomUUID()
    Spread(
      id = SpreadId(id),
      title = externalSpread.title,
      cardCount = externalSpread.cardCount,
      spreadStatus = SpreadStatus.Draft,
      coverPhoto = Photo.toPhotoSource(storedPhoto, PhotoOwnerType.Spread, id),
      createdAt = DateTimeService.getDateTimeNow,
      scheduledAt = None,
      publishedAt = None
    )
  }
}