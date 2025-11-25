package tarot.api.dto.tarot.photo

import shared.api.dto.tarot.photo.PhotoResponse
import tarot.domain.models.TarotError
import tarot.domain.models.photo.Photo

object PhotoResponseMapper {
  def toResponse(photo: Photo): PhotoResponse =
    photo match {
      case Photo.Local(_, ownerType, ownerId, fileId) =>
          PhotoResponse(
            ownerType = ownerType,
            ownerId = ownerId,
            fileId = fileId
          )
      case Photo.S3(_, _, ownerType, ownerId, fileId) =>
        PhotoResponse(
          ownerType = ownerType,
          ownerId = ownerId,
          fileId = fileId
        )
    }


}