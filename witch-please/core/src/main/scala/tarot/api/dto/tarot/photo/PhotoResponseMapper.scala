package tarot.api.dto.tarot.photo

import shared.api.dto.tarot.photo.{PhotoRequest, PhotoResponse}
import shared.api.dto.tarot.spreads.SpreadRequest
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.photo.Photo
import zio.ZIO

object PhotoResponseMapper {
  def toResponse(photo: Photo): PhotoResponse =
    photo match {
      case Photo.Local(_, ownerType, ownerId, sourceType, fileId) =>
        PhotoResponse(
          ownerType = ownerType,
          ownerId = ownerId,
          sourceType = sourceType,
          fileId = fileId
        )
      case Photo.S3(_, _, ownerType, ownerId, sourceType, fileId) =>
        PhotoResponse(
          ownerType = ownerType,
          ownerId = ownerId,
          sourceType = sourceType,
          fileId = fileId
        )
    }
}