package tarot.domain.entities

import shared.models.tarot.spreads.SpreadStatus
import tarot.domain.models.spreads.Spread

import java.time.Instant
import java.util.UUID

final case class SpreadEntity(
  id: UUID,
  projectId: UUID,
  title: String,
  cardCount: Int,
  description: String,
  status: SpreadStatus,
  photoId: UUID,
  createdAt: Instant,
  scheduledAt: Option[Instant],
  publishedAt: Option[Instant]
)

object SpreadEntity {
  def toEntity(spread: Spread): SpreadEntity =
    SpreadEntity(
      id = spread.id.id,
      projectId = spread.projectId.id,
      title = spread.title,
      cardCount = spread.cardsCount,
      description = spread.description,
      status = spread.status,
      photoId = spread.photo.id.id,
      createdAt = spread.createdAt,
      scheduledAt = spread.scheduledAt,
      publishedAt = spread.publishedAt
    )
}
