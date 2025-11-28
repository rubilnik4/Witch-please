package tarot.domain.models.photo

import shared.models.files.*
import shared.models.tarot.photo.PhotoOwnerType

import java.util.UUID

sealed trait Photo(
  ownerType: PhotoOwnerType, 
  ownerId: UUID,
  sourceType: FileSourceType,                
  fileId: String   
)

object Photo {
  case class Local(
    path: String,
    ownerType: PhotoOwnerType,
    ownerId: UUID,
    sourceType: FileSourceType,
    fileId: String
  ) extends Photo(ownerType, ownerId, sourceType, fileId)

  case class S3(
    bucket: String,
    key: String,
    ownerType: PhotoOwnerType,
    ownerId: UUID,
    sourceType: FileSourceType,           
    fileId: String
  ) extends Photo(ownerType, ownerId, sourceType, fileId)

  def toPhoto(storageType: FileStorage, ownerType: PhotoOwnerType,
              ownerId: UUID, sourceType: FileSourceType, fileId: String): Photo =
    storageType match {
      case FileStorage.Local(path) =>
        Photo.Local(path, ownerType, ownerId, sourceType, fileId)
      case FileStorage.S3(bucket, key) =>
        Photo.S3(bucket, key, ownerType, ownerId, sourceType, fileId)
    }
}