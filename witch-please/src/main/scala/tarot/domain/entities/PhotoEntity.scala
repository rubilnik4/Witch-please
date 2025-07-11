package tarot.domain.entities

import tarot.domain.models.TarotError
import tarot.domain.models.photo.{PhotoOwnerType, Photo, PhotoStorageType}
import zio.ZIO


import java.util.UUID

final case class PhotoEntity(
    id: UUID,
    storageType: PhotoStorageType,
    ownerType: PhotoOwnerType,
    ownerId: UUID,
    path: Option[String],
    bucket: Option[String],
    key: Option[String])

object PhotoEntity {
  def toDomain(photoSource: PhotoEntity): ZIO[Any, TarotError, Photo] =
    photoSource.storageType match {
      case PhotoStorageType.Local =>
        for {
          path <- ZIO.fromOption(photoSource.path)
            .orElseFail(TarotError.SerializationError("Missing 'path' for Local photo source"))
        } yield Photo.Local(path, photoSource.ownerType, photoSource.ownerId)

      case PhotoStorageType.S3 =>
        for {
          bucket <- ZIO.fromOption(photoSource.bucket)
            .orElseFail(TarotError.SerializationError("Missing 'bucket' for S3 photo source"))
          key <- ZIO.fromOption(photoSource.key)
            .orElseFail(TarotError.SerializationError("Missing 'key' for S3 photo source"))
        } yield Photo.S3(bucket, key, photoSource.ownerType, photoSource.ownerId)
    }


  def toEntity(photoSource: Photo): PhotoEntity =
    photoSource match {
      case Photo.Local(path, ownerType, ownerId) =>
        PhotoEntity(UUID.randomUUID(), PhotoStorageType.Local, ownerType, ownerId, Some(path), None, None)
      case Photo.S3(bucket, key, ownerType, ownerId) =>
        PhotoEntity(UUID.randomUUID(), PhotoStorageType.S3, ownerType, ownerId, None, Some(bucket), Some(key))
    }
}
