package tarot.domain.entities

import tarot.domain.models.TarotError
import tarot.domain.models.cards.{Card, CardId}
import tarot.domain.models.spreads.SpreadId
import zio.ZIO

import java.time.Instant
import java.util.UUID

final case class CardEntity(
  id: UUID,
  spreadId: UUID,
  description: String,
  coverPhotoId: UUID,
  createdAt: Instant
)

object CardEntity {
  def toEntity(card: Card, coverPhotoId: UUID): CardEntity =
    CardEntity(
      id = card.id.id,
      spreadId = card.spreadId.id,
      description = card.description,
      coverPhotoId = coverPhotoId,
      createdAt = card.createdAt
    )
}
