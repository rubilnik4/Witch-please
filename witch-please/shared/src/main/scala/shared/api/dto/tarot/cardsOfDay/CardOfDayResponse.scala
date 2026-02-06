package shared.api.dto.tarot.cardsOfDay

import shared.api.dto.tarot.photo.PhotoResponse
import shared.models.tarot.cardOfDay.CardOfDayStatus
import zio.json.*
import zio.schema.*

import java.time.Instant
import java.util.UUID

final case class CardOfDayResponse(
  id: UUID,
  cardId: UUID,
  spreadId: UUID,
  title: String,
  description: String,
  status: CardOfDayStatus,
  photo: PhotoResponse,
  createdAt: Instant,
  scheduledAt: Option[Instant],
  publishedAt: Option[Instant]
) derives JsonCodec, Schema