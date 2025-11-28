package tarot.domain.entities

import shared.models.files.*
import shared.models.tarot.photo.PhotoOwnerType
import tarot.domain.models.TarotError
import tarot.domain.models.photo.Photo
import zio.ZIO

import java.util.UUID

final case class PhotoEntity(
  id: UUID,
  ownerType: PhotoOwnerType,
  ownerId: UUID,
  storageType: FileStorageType,
  sourceType: FileSourceType,
  fileId: String,
  path: Option[String],
  bucket: Option[String],
  key: Option[String]
)

object PhotoEntity {
  def toDomain(photoSource: PhotoEntity): ZIO[Any, TarotError, Photo] =
    photoSource.storageType match {
      case FileStorageType.Local =>
        for {
          path <- ZIO.fromOption(photoSource.path)
            .orElseFail(TarotError.SerializationError("Missing 'path' for Local photo source"))
        } yield Photo.Local(path, photoSource.ownerType, photoSource.ownerId, photoSource.sourceType, photoSource.fileId)

      case FileStorageType.S3 =>
        for {
          bucket <- ZIO.fromOption(photoSource.bucket)
            .orElseFail(TarotError.SerializationError("Missing 'bucket' for S3 photo source"))
          key <- ZIO.fromOption(photoSource.key)
            .orElseFail(TarotError.SerializationError("Missing 'key' for S3 photo source"))
        } yield Photo.S3(bucket, key, photoSource.ownerType, photoSource.ownerId, photoSource.sourceType, photoSource.fileId)
    }


  def toEntity(photoSource: Photo): PhotoEntity =
    photoSource match {
      case Photo.Local(path, ownerType, ownerId, sourceType, fileId) =>
        PhotoEntity(
          id = UUID.randomUUID(),
          ownerType = ownerType,
          ownerId = ownerId,
          storageType = FileStorageType.Local,
          sourceType = sourceType,
          fileId = fileId,
          path = Some(path),
          bucket = None,
          key = None
        )

      case Photo.S3(bucket, key, ownerType, ownerId, sourceType, fileId) =>
        PhotoEntity(
          id = UUID.randomUUID(),
          ownerType = ownerType,
          ownerId = ownerId,
          storageType = FileStorageType.S3,
          sourceType = sourceType,
          fileId = fileId,
          path = None,
          bucket = Some(bucket),
          key = Some(key)
        )
    }
}
