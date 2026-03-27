package tarot.domain.entities

import shared.models.files.{FileStoredType, FileStored}
import tarot.domain.models.TarotError
import tarot.domain.models.photo.PhotoObject
import zio.ZIO

import java.util.UUID

final case class PhotoObjectEntity(
  id: UUID,
  fileId: UUID,
  hash: String,
  storageType: FileStoredType,
  path: Option[String],
  bucket: Option[String],
  key: Option[String]
)

object PhotoObjectEntity {
  def toDomain(photoObjectEntity: PhotoObjectEntity): ZIO[Any, TarotError, PhotoObject] =
    toDomain(
      fileId = photoObjectEntity.fileId,
      hash = photoObjectEntity.hash,
      storageType = photoObjectEntity.storageType,
      path = photoObjectEntity.path,
      bucket = photoObjectEntity.bucket,
      key = photoObjectEntity.key
    )

  def toDomain(
    fileId: UUID,
    hash: String,
    storageType: FileStoredType,
    path: Option[String],
    bucket: Option[String],
    key: Option[String]
  ): ZIO[Any, TarotError, PhotoObject] =
    storageType match {
      case FileStoredType.Local =>
        for {
          path <- ZIO.fromOption(path)
            .orElseFail(TarotError.SerializationError("Missing 'path' for Local photo object"))
        } yield PhotoObject.Local(
          fileId = fileId,
          hash = hash,
          path = path
        )
      case FileStoredType.S3 =>
        for {
          bucket <- ZIO.fromOption(bucket)
            .orElseFail(TarotError.SerializationError("Missing 'bucket' for S3 photo object"))
          key <- ZIO.fromOption(key)
            .orElseFail(TarotError.SerializationError("Missing 'key' for S3 photo object"))
        } yield PhotoObject.S3(
          fileId = fileId,
          hash = hash,
          bucket = bucket,
          key = key
        )
    }

  def toEntity(id: UUID, photoObject: PhotoObject): PhotoObjectEntity =
    photoObject match {
      case PhotoObject.Local(fileId, hash, path) =>
        PhotoObjectEntity(
          id = id,
          fileId = fileId,
          hash = hash,
          storageType = FileStoredType.Local,
          path = Some(path),
          bucket = None,
          key = None
        )
      case PhotoObject.S3(fileId, hash, bucket, key) =>
        PhotoObjectEntity(
          id = id,
          fileId = fileId,
          hash = hash,
          storageType = FileStoredType.S3,
          path = None,
          bucket = Some(bucket),
          key = Some(key)
        )
    }
}
