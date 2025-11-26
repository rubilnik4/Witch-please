package tarot.domain.entities

import shared.models.tarot.spreads.SpreadStatus
import tarot.domain.models.TarotError
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.{Spread, SpreadId}
import zio.ZIO

import java.time.Instant
import java.util.UUID

final case class SpreadEntity(
  id: UUID,
  projectId: UUID,
  title: String,
  cardCount: Int,
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
      cardCount = spread.cardCount,
      spreadStatus = spread.spreadStatus,
      photoId = coverPhotoId,
      createdAt = spread.createdAt,
      scheduledAt = spread.scheduledAt,
      cardOfDayAt = Spread.getCardOfDayAt(spread),
      publishedAt = spread.publishedAt
    )
}
