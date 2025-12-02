package shared.api.dto.tarot.photo

import shared.models.files.FileSourceType
import zio.json.*
import zio.schema.*

final case class PhotoRequest(
  sourceType: FileSourceType,
  sourceId: String
) derives JsonCodec, Schema
