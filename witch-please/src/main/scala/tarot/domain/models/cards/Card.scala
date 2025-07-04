package tarot.domain.models.cards

import tarot.domain.models.contracts.SpreadId
import tarot.domain.models.photo.{PhotoFile, PhotoOwnerType, Photo, PhotoSource}
import tarot.domain.models.spreads.Card
import tarot.infrastructure.services.common.DateTimeService
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

import java.time.Instant
import java.util.UUID

final case class Card(
                       id: UUID,
                       spreadId: SpreadId,
                       description: String,
                       coverPhoto: Photo,
                       time: Instant)
{
  override def toString: String = s"card id: $id; spreadId:$spreadId"
}

object CardMapper {
  def fromExternal(externalCard: ExternalCard, storedPhoto: PhotoSource): Card =
    val id = UUID.randomUUID()
    Card(
      id = id,
      spreadId = externalCard.spreadId,
      description = externalCard.description,
      coverPhoto = Photo.toPhotoSource(storedPhoto, PhotoOwnerType.Card, id),
      time = DateTimeService.getDateTimeNow
    )
}