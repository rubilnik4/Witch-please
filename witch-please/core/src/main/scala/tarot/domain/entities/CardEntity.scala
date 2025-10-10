package tarot.domain.entities

import tarot.domain.models.cards.Card

import java.time.Instant
import java.util.UUID

final case class CardEntity(
  id: UUID,
  index: Int,
  spreadId: UUID,
  description: String,
  photoId: UUID,
  createdAt: Instant
)

object CardEntity {
  def toEntity(card: Card, coverPhotoId: UUID): CardEntity =
    CardEntity(
      id = card.id.id,
      index = card.index,
      spreadId = card.spreadId.id,
      description = card.description,
      photoId = coverPhotoId,
      createdAt = card.createdAt
    )
}
