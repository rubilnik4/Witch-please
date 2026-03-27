package tarot.api.dto.tarot.photo

import shared.api.dto.tarot.photo.PhotoResponse
import tarot.domain.models.photo.Photo

object PhotoResponseMapper {
  def toResponse(photo: Photo): PhotoResponse =
    PhotoResponse(
      id = photo.id.id,
      fileId = photo.photoObject.fileId,
      sourceType = photo.sourceType,
      sourceId = photo.sourceId
    )
}
