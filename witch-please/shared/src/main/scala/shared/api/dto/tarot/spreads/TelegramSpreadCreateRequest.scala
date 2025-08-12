package shared.api.dto.tarot.spreads

import zio.json.*
import zio.schema.*

import java.util.UUID

final case class TelegramSpreadCreateRequest(
  projectId: UUID,
  title: String,
  cardCount: Int,
  coverPhotoId: String
) derives JsonCodec, Schema