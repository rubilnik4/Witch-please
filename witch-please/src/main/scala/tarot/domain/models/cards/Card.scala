package tarot.domain.models.cards

import tarot.domain.models.photo.*
import tarot.domain.models.spreads.SpreadId
import tarot.infrastructure.services.common.DateTimeService

import java.time.Instant
import java.util.UUID

final case class Card(
  id: CardId,
  spreadId: SpreadId,
  description: String,
  coverPhoto: Photo,
  createdAt: Instant
)
{
  override def toString: String = s"card id: $id; spreadId:$spreadId"
}

object CardMapper {
  def fromExternal(externalCard: ExternalCard, storedPhoto: PhotoSource): Card =
    val id = UUID.randomUUID()
    Card(
      id = CardId(id),
      spreadId = externalCard.spreadId,
      description = externalCard.description,
      coverPhoto = Photo.toPhotoSource(storedPhoto, PhotoOwnerType.Card, id),
      createdAt = DateTimeService.getDateTimeNow
    )
}