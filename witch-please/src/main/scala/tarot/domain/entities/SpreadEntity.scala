package tarot.domain.entities

import tarot.domain.models.TarotError
import tarot.domain.models.spreads.{Spread, SpreadId, SpreadStatus}
import zio.ZIO

import java.time.Instant
import java.util.UUID

final case class SpreadEntity(
    id: UUID,
    title: String,
    cardCount: Int,
    spreadStatus: SpreadStatus,
    coverPhotoId: UUID,
    createdAt: Instant,
    scheduledAt: Option[Instant],
    publishedAt: Option[Instant]
)

object SpreadEntity {
  def toEntity(spread: Spread, coverPhotoId: UUID): SpreadEntity =
    SpreadEntity(
      id = spread.id.id,
      title = spread.title,
      cardCount = spread.cardCount,
      spreadStatus = spread.spreadStatus,
      coverPhotoId = coverPhotoId,
      createdAt = spread.createdAt,
      scheduledAt = spread.scheduledAt,
      publishedAt = spread.publishedAt
    )
}
