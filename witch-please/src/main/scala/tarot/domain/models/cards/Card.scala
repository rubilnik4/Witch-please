package tarot.domain.models.cards

import tarot.domain.models.contracts.SpreadId
import tarot.domain.models.photo.{PhotoFile, PhotoOwnerType, PhotoSource, StoredPhotoSource}
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
    coverPhoto: PhotoSource,
    time: Instant)
{
  override def toString: String = s"card id: $id; spreadId:$spreadId"
}

object CardMapper {
  def fromExternal(externalCard: ExternalCard, storedPhoto: StoredPhotoSource): Card =
    val id = UUID.randomUUID()
    Card(
      id = id,
      spreadId = externalCard.spreadId,
      description = externalCard.description,
      coverPhoto = PhotoSource.toPhotoSource(storedPhoto, PhotoOwnerType.Card, id),
      time = DateTimeService.getDateTimeNow
    )
}