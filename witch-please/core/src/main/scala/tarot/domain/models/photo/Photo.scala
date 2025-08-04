package tarot.domain.models.photo

import java.util.UUID

sealed trait Photo(ownerType: PhotoOwnerType, ownerId: UUID)

object Photo:
  final case class Local(path: String, ownerType: PhotoOwnerType, ownerId: UUID)
    extends Photo(ownerType, ownerId)

  final case class S3(bucket: String, key: String, ownerType: PhotoOwnerType, ownerId: UUID)
    extends Photo(ownerType, ownerId)

  def toPhotoSource(stored: PhotoSource, ownerType: PhotoOwnerType, ownerId: UUID): Photo = 
    stored match {
      case PhotoSource.Local(path) =>
        Photo.Local(path, ownerType, ownerId)
  
      case PhotoSource.S3(bucket, key) =>
        Photo.S3(bucket, key, ownerType, ownerId)
    }