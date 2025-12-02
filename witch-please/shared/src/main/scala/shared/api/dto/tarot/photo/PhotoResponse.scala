package shared.api.dto.tarot.photo

import shared.models.files.FileSourceType
import shared.models.tarot.photo.PhotoOwnerType
import zio.json.*
import zio.schema.*

import java.util.UUID

final case class PhotoResponse(
  id: UUID,
  fileId: UUID,
  ownerType: PhotoOwnerType,
  ownerId: UUID,
  sourceType: FileSourceType,
  sourceId: String
) derives JsonCodec, Schema