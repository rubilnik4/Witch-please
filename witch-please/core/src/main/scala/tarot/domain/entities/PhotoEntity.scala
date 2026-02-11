package tarot.domain.entities

import shared.models.files.*
import shared.models.tarot.photo.PhotoOwnerType
import tarot.domain.models.TarotError
import tarot.domain.models.photo.{Photo, PhotoId}
import zio.ZIO

import java.util.UUID

final case class PhotoEntity(
                              id: UUID,
                              fileId: UUID,
                              ownerType: PhotoOwnerType,
                              ownerId: UUID,
                              storageType: FileStoredType,
                              sourceType: FileSourceType,
                              sourceId: String,
                              path: Option[String],
                              bucket: Option[String],
                              key: Option[String]
  )

object PhotoEntity {
  def toDomain(photoSource: PhotoEntity): ZIO[Any, TarotError, Photo] =
    photoSource.storageType match {
      case FileStoredType.Local =>
        for {
          path <- ZIO.fromOption(photoSource.path)
            .orElseFail(TarotError.SerializationError("Missing 'path' for Local photo source"))
        } yield Photo.Local(PhotoId(photoSource.id), photoSource.fileId, path,
          photoSource.ownerType, photoSource.ownerId, photoSource.sourceType, photoSource.sourceId)

      case FileStoredType.S3 =>
        for {
          bucket <- ZIO.fromOption(photoSource.bucket)
            .orElseFail(TarotError.SerializationError("Missing 'bucket' for S3 photo source"))
          key <- ZIO.fromOption(photoSource.key)
            .orElseFail(TarotError.SerializationError("Missing 'key' for S3 photo source"))
        } yield Photo.S3(PhotoId(photoSource.id), photoSource.fileId, bucket, key,
          photoSource.ownerType, photoSource.ownerId, photoSource.sourceType, photoSource.sourceId)
    }


  def toEntity(photoSource: Photo): PhotoEntity =
    photoSource match {
      case Photo.Local(id, fileId, path, ownerType, ownerId, sourceType, sourceId) =>
        PhotoEntity(
          id = id.id,
          fileId = fileId,
          ownerType = ownerType,
          ownerId = ownerId,
          storageType = FileStoredType.Local,
          sourceType = sourceType,
          sourceId = sourceId,
          path = Some(path),
          bucket = None,
          key = None
        )

      case Photo.S3(id, fileId, bucket, key, ownerType, ownerId, sourceType, sourceId) =>
        PhotoEntity(
          id = id.id,
          fileId = fileId,
          ownerType = ownerType,
          ownerId = ownerId,
          storageType = FileStoredType.S3,
          sourceType = sourceType,
          sourceId = sourceId,
          path = None,
          bucket = Some(bucket),
          key = Some(key)
        )
    }
}
