package shared.api.dto.tarot.photo

import shared.models.files.FileSourceType
import shared.models.tarot.photo.PhotoOwnerType
import zio.json.*
import zio.schema.*

import java.util.UUID

final case class PhotoResponse(
  ownerType: PhotoOwnerType,
  ownerId: UUID,
  sourceType: FileSourceType,
  fileId: String
) derives JsonCodec, Schema