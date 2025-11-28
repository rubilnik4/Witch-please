package shared.api.dto.tarot.spreads

import shared.api.dto.tarot.photo.PhotoRequest
import zio.json.*
import zio.schema.*

import java.util.UUID

final case class SpreadCreateRequest(
  title: String,
  cardCount: Int,
  photo: PhotoRequest
) extends SpreadRequest derives JsonCodec, Schema