package shared.api.dto.tarot.cards

import shared.api.dto.tarot.photo.PhotoRequest
import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

import java.util.UUID

final case class CardCreateRequest(
  position: Int,
  title: String,
  photo: PhotoRequest
) derives JsonCodec, Schema