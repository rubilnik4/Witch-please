package shared.api.dto.tarot.photo

import shared.models.files.FileSourceType
import zio.json.*
import zio.schema.*

import java.util.UUID

final case class PhotoResponse(
  id: UUID,
  fileId: UUID,
  sourceType: FileSourceType,
  sourceId: String
) derives JsonCodec, Schema
