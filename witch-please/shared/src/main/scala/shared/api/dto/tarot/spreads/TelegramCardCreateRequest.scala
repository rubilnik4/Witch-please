package shared.api.dto.tarot.spreads

import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

import java.util.UUID

final case class TelegramCardCreateRequest(
  description: String,
  coverPhotoId: String
) derives JsonCodec, Schema