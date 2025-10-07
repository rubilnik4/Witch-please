package shared.api.dto.tarot.spreads

import shared.api.dto.tarot.photo.PhotoResponse
import shared.models.tarot.spreads.SpreadStatus
import zio.json.*
import zio.schema.*

import java.time.Instant
import java.util.UUID

final case class SpreadResponse(
  id: UUID,
  projectId: UUID,
  title: String,
  cardCount: Int,
  spreadStatus: SpreadStatus,
  coverPhoto: PhotoResponse,
  createdAt: Instant,
  scheduledAt: Option[Instant],
  publishedAt: Option[Instant]
) derives JsonCodec, Schema