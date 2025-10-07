package tarot.api.dto.tarot.photo

import shared.api.dto.tarot.photo.PhotoResponse
import shared.api.dto.tarot.projects.ProjectResponse
import shared.api.dto.tarot.spreads.SpreadResponse
import shared.api.dto.tarot.users.*
import shared.models.tarot.authorize.ClientType
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.authorize.{ExternalUser, User}
import tarot.domain.models.photo.Photo
import tarot.domain.models.projects.Project
import tarot.domain.models.spreads.Spread
import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

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