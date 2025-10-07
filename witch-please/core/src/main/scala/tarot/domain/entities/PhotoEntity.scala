package tarot.domain.entities

import shared.models.tarot.photo.PhotoOwnerType
import tarot.domain.models.TarotError
import tarot.domain.models.photo.{Photo, PhotoStorageType}
import zio.ZIO


import java.util.UUID

final case class PhotoEntity(
  id: UUID,
  storageType: PhotoStorageType,
  ownerType: PhotoOwnerType,
  ownerId: UUID,
  path: Option[String],
  bucket: Option[String],
  key: Option[String],
  fileId: Option[String]
)

object PhotoEntity {
  def toDomain(photoSource: PhotoEntity): ZIO[Any, TarotError, Photo] =
    photoSource.storageType match {
      case PhotoStorageType.Local =>
        for {
          path <- ZIO.fromOption(photoSource.path)
            .orElseFail(TarotError.SerializationError("Missing 'path' for Local photo source"))
        } yield Photo.Local(path, photoSource.ownerType, photoSource.ownerId, photoSource.fileId)

      case PhotoStorageType.S3 =>
        for {
          bucket <- ZIO.fromOption(photoSource.bucket)
            .orElseFail(TarotError.SerializationError("Missing 'bucket' for S3 photo source"))
          key <- ZIO.fromOption(photoSource.key)
            .orElseFail(TarotError.SerializationError("Missing 'key' for S3 photo source"))
        } yield Photo.S3(bucket, key, photoSource.ownerType, photoSource.ownerId, photoSource.fileId)
    }


  def toEntity(photoSource: Photo): PhotoEntity =
    photoSource match {
      case Photo.Local(path, ownerType, ownerId, fileId) =>
        PhotoEntity(
          id = UUID.randomUUID(),
          storageType = PhotoStorageType.Local,
          ownerType = ownerType,
          ownerId = ownerId,
          path = Some(path),
          bucket = None,
          key = None,
          fileId = fileId
        )

      case Photo.S3(bucket, key, ownerType, ownerId, fileId) =>
        PhotoEntity(
          id = UUID.randomUUID(),
          storageType = PhotoStorageType.S3,
          ownerType = ownerType,
          ownerId = ownerId,
          path = None,
          bucket = Some(bucket),
          key = Some(key),
          fileId = fileId
        )
    }
}
