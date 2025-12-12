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
  spreadStatus: SpreadStatus,
  photoId: UUID,
  createdAt: Instant,
  scheduledAt: Option[Instant],
  cardOfDayAt: Option[Instant],                    
  publishedAt: Option[Instant]
)

object SpreadEntity {
  def toEntity(spread: Spread, coverPhotoId: UUID): SpreadEntity =
    SpreadEntity(
      id = spread.id.id,
      projectId = spread.projectId.id,
      title = spread.title,
      cardCount = spread.cardsCount,
      description = spread.description,
      spreadStatus = spread.spreadStatus,
      photoId = coverPhotoId,
      createdAt = spread.createdAt,
      scheduledAt = spread.scheduledAt,
      cardOfDayAt = Spread.getCardOfDayAt(spread.scheduledAt, spread.cardOfDayDelay),
      publishedAt = spread.publishedAt
    )
}
