package shared.api.dto.tarot.spreads

import shared.api.dto.tarot.photo.PhotoRequest
import zio.json.*
import zio.schema.*

import java.time.{Duration, Instant}
import java.util.UUID

final case class SpreadUpdateRequest(
  title: String,
  cardCount: Int,
  description: String,
  photo: PhotoRequest
) extends SpreadRequest derives JsonCodec, Schema