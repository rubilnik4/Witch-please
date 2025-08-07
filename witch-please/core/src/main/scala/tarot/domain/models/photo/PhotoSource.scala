package tarot.domain.models.photo

sealed trait PhotoSource

object PhotoSource:
  final case class Local(path: String) extends PhotoSource
  final case class S3(bucket: String, key: String) extends PhotoSource
