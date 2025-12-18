package shared.api.dto.tarot.cards

import shared.api.dto.tarot.photo.PhotoRequest
import zio.json.*
import zio.schema.*

import java.util.UUID

final case class CardOfDayCreateRequest(
  cardId: UUID,
  description: String,           
  photo: PhotoRequest
) derives JsonCodec, Schema