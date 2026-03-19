package tarot.domain.models.photo

import shared.models.files.*
import shared.models.photo.PhotoSource
import java.util.UUID

sealed trait Photo {
  def id: PhotoId
  def fileId: UUID
  def sourceType: FileSourceType
  def sourceId: String
}

object Photo {
  case class Local(
    id: PhotoId,
    fileId: UUID,
    path: String,
    sourceType: FileSourceType,
    sourceId: String
  ) extends Photo

  case class S3(
   id: PhotoId,
   fileId: UUID,
   bucket: String,
   key: String,
   sourceType: FileSourceType,
   sourceId: String
  ) extends Photo

  def toPhoto(id: UUID, photoFile: FileStored, sourceType: FileSourceType, sourceId: String): Photo =
    photoFile match {
      case FileStored.Local(fileId, path) =>
        Photo.Local(PhotoId(id), fileId, path, sourceType, sourceId)
      case FileStored.S3(fileId, bucket, key) =>
        Photo.S3(PhotoId(id), fileId, bucket, key, sourceType, sourceId)
    }

  def toPhotoSource(photo: Photo): PhotoSource =
    toPhotoSource(photo, None)
    
  def toPhotoSource(photo: Photo, parentId: Option[String]): PhotoSource =
    PhotoSource(photo.sourceId, photo.sourceType, parentId)  
}
