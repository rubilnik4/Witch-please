package shared.api.dto.tarot.cards

import shared.api.dto.tarot.photo.PhotoResponse
import zio.json.*
import zio.schema.*

import java.time.Instant
import java.util.UUID

final case class CardResponse(
  id: UUID,
  position: Int,
  spreadId: UUID,
  description: String,
  photo: PhotoResponse,
  createdAt: Instant
) derives JsonCodec, Schema