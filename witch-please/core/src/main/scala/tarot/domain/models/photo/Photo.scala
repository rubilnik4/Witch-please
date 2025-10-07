package tarot.domain.models.photo

import shared.models.files.FileSource
import shared.models.tarot.photo.PhotoOwnerType

import java.util.UUID

sealed trait Photo(
  ownerType: PhotoOwnerType, 
  ownerId: UUID,
  externalFileId: Option[String]    
)

object Photo {
  case class Local(
    path: String,
    ownerType: PhotoOwnerType,
    ownerId: UUID,
    fileId: Option[String]
  ) extends Photo(ownerType, ownerId, fileId)

  case class S3(
    bucket: String,
    key: String,
    ownerType: PhotoOwnerType,
    ownerId: UUID,
    fileId: Option[String]
  ) extends Photo(ownerType, ownerId, fileId)

  def toPhoto(stored: FileSource, ownerType: PhotoOwnerType, ownerId: UUID, fileId: Option[String]): Photo =
    stored match {
      case FileSource.Local(path) =>
        Photo.Local(path, ownerType, ownerId, fileId)

      case FileSource.S3(bucket, key) =>
        Photo.S3(bucket, key, ownerType, ownerId, fileId)
    }
}