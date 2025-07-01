package tarot.domain.models.photo

import java.util.UUID

sealed trait PhotoSource(ownerType: PhotoOwnerType, ownerId: UUID)

object PhotoSource:
  final case class Local(path: String, ownerType: PhotoOwnerType, ownerId: UUID)
    extends PhotoSource(ownerType, ownerId)

  final case class S3(bucket: String, key: String, ownerType: PhotoOwnerType, ownerId: UUID)
    extends PhotoSource(ownerType, ownerId)

  def toPhotoSource(stored: StoredPhotoSource, ownerType: PhotoOwnerType, ownerId: UUID): PhotoSource = 
    stored match {
      case StoredPhotoSource.Local(path) =>
        PhotoSource.Local(path, ownerType, ownerId)
  
      case StoredPhotoSource.S3(bucket, key) =>
        PhotoSource.S3(bucket, key, ownerType, ownerId)
    }