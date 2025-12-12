package shared.api.dto.tarot.spreads

import shared.api.dto.tarot.photo.PhotoRequest
import zio.json.*
import zio.schema.*

final case class SpreadCreateRequest(
  title: String,
  cardCount: Int,
  description: String,
  photo: PhotoRequest
) extends SpreadRequest derives JsonCodec, Schema