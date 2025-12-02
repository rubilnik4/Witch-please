package tarot.api.dto.tarot.photo

import shared.api.dto.tarot.photo.PhotoResponse
import tarot.domain.models.photo.Photo

object PhotoResponseMapper {
  def toResponse(photo: Photo): PhotoResponse =
    photo match {
      case Photo.Local(id, fileId, _, ownerType, ownerId, sourceType, sourceId) =>
        PhotoResponse(
          id = id.id,
          fileId = fileId,
          ownerType = ownerType,
          ownerId = ownerId,
          sourceType = sourceType,
          sourceId = sourceId
        )
      case Photo.S3(id, fileId, _, _, ownerType, ownerId, sourceType, sourceId) =>
        PhotoResponse(
          id = id.id,
          fileId = fileId,
          ownerType = ownerType,
          ownerId = ownerId,
          sourceType = sourceType,
          sourceId = sourceId
        )
    }
}