package shared.api.dto.tarot.cards

import shared.api.dto.tarot.photo.PhotoRequest
import zio.json.*
import zio.schema.*

final case class CardUpdateRequest(
  title: String,
  photo: PhotoRequest
) derives JsonCodec, Schema