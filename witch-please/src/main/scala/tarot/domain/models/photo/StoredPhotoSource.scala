package tarot.domain.models.photo

import java.util.UUID

sealed trait StoredPhotoSource

object StoredPhotoSource:
  final case class Local(path: String) extends StoredPhotoSource
  final case class S3(bucket: String, key: String) extends StoredPhotoSource
