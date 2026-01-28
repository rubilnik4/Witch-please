package tarot.domain.entities

import shared.models.tarot.cardOfDay.CardOfDayStatus
import tarot.domain.models.cardsOfDay.CardOfDay
import tarot.domain.models.cards.Card

import java.time.Instant
import java.util.UUID

final case class CardOfDayEntity(
  id: UUID,
  cardId: UUID,
  spreadId: UUID,
  title: String,
  description: String,
  status: CardOfDayStatus,
  photoId: UUID,
  createdAt: Instant,
  scheduledAt: Option[Instant],
  publishedAt: Option[Instant]
)

object CardOfDayEntity {
  def toEntity(cardOfDay: CardOfDay, coverPhotoId: UUID): CardOfDayEntity =
    CardOfDayEntity(
      id = cardOfDay.id.id,
      cardId = cardOfDay.cardId.id,
      spreadId = cardOfDay.spreadId.id,
      title = cardOfDay.title,
      description = cardOfDay.description,
      status = cardOfDay.status,
      photoId = coverPhotoId,
      createdAt = cardOfDay.createdAt,
      scheduledAt = cardOfDay.scheduledAt,
      publishedAt = cardOfDay.publishedAt
    )
}
