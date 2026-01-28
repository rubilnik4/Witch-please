package shared.api.dto.tarot.cardsOfDay

import shared.api.dto.tarot.photo.PhotoRequest
import zio.json.*
import zio.schema.*

import java.util.UUID

final case class CardOfDayUpdateRequest(
  cardId: UUID,
  title: String,
  description: String,
  photo: PhotoRequest
) extends CardOfDayRequest derives JsonCodec, Schema