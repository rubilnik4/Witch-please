package tarot.domain.entities

import tarot.domain.models.cards.Card

import java.time.Instant
import java.util.UUID

final case class CardEntity(
  id: UUID,
  position: Int,
  spreadId: UUID,
  title: String,
  description: String,
  photoId: UUID,
  createdAt: Instant
)

object CardEntity {
  def toEntity(card: Card): CardEntity =
    CardEntity(
      id = card.id.id,
      position = card.position,
      spreadId = card.spreadId.id,
      title = card.title,
      description = card.description,
      photoId = card.photo.id.id,
      createdAt = card.createdAt
    )
}
