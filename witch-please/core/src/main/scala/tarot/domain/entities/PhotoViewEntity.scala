package tarot.domain.entities

import shared.models.files.{FileSourceType, FileStoredType}
import tarot.domain.models.TarotError
import tarot.domain.models.photo.{Photo, PhotoId}
import zio.ZIO

import java.util.UUID

final case class PhotoViewEntity(
  id: UUID,
  sourceType: FileSourceType,
  sourceId: String,
  fileId: UUID,
  hash: String,
  storageType: FileStoredType,
  path: Option[String],
  bucket: Option[String],
  key: Option[String]
)

object PhotoViewEntity {
  def toDomain(photoViewEntity: PhotoViewEntity): ZIO[Any, TarotError, Photo] =
    for {
      photoObject <- PhotoObjectEntity.toDomain(
        fileId = photoViewEntity.fileId,
        hash = photoViewEntity.hash,
        storageType = photoViewEntity.storageType,
        path = photoViewEntity.path,
        bucket = photoViewEntity.bucket,
        key = photoViewEntity.key
      )
    } yield Photo(
      id = PhotoId(photoViewEntity.id),
      photoObject = photoObject,
      sourceType = photoViewEntity.sourceType,
      sourceId = photoViewEntity.sourceId
    )
}
