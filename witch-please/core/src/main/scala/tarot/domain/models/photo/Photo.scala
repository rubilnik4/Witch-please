package tarot.domain.models.photo

import shared.models.files.*
import shared.models.tarot.photo.PhotoOwnerType

import java.util.UUID

sealed trait Photo {
  def id: PhotoId
  def ownerType: PhotoOwnerType
  def ownerId: UUID
  def fileId: UUID
  def sourceType: FileSourceType
  def sourceId: String
}

object Photo {
  case class Local(
    id: PhotoId,
    fileId: UUID,
    path: String,
    ownerType: PhotoOwnerType,
    ownerId: UUID,
    sourceType: FileSourceType,
    sourceId: String
  ) extends Photo

  case class S3(
   id: PhotoId,
   fileId: UUID,
   bucket: String,
   key: String,
   ownerType: PhotoOwnerType,
   ownerId: UUID,
   sourceType: FileSourceType,
   sourceId: String
  ) extends Photo

  def toPhoto(id: UUID, photoFile: FileStorage, ownerType: PhotoOwnerType,
              ownerId: UUID, sourceType: FileSourceType, sourceId: String): Photo =
    photoFile match {
      case FileStorage.Local(fileId, path) =>
        Photo.Local(PhotoId(id), fileId, path, ownerType, ownerId, sourceType, sourceId)
      case FileStorage.S3(fileId, bucket, key) =>
        Photo.S3(PhotoId(id), fileId, bucket, key, ownerType, ownerId, sourceType, sourceId)
    }
}