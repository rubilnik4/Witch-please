package tarot.domain.models.photo

import shared.models.files.FileStored

import java.util.UUID

sealed trait PhotoObject {
  def fileId: UUID
  def hash: String
}

object PhotoObject {
  final case class Local(
    fileId: UUID,
    hash: String,
    path: String
  ) extends PhotoObject

  final case class S3(
    fileId: UUID,
    hash: String,
    bucket: String,
    key: String
  ) extends PhotoObject

  def toPhotoObject(hash: String, fileStored: FileStored): PhotoObject =
    fileStored match {
      case FileStored.Local(fileId, path) =>
        Local(fileId, hash, path)
      case FileStored.S3(fileId, bucket, key) =>
        S3(fileId, hash, bucket, key)
    }

  def toFileStored(photoObject: PhotoObject): FileStored =
    photoObject match {
      case Local(fileId, _, path) =>
        FileStored.Local(fileId, path)
      case S3(fileId, _, bucket, key) =>
        FileStored.S3(fileId, bucket, key)
    }
}
