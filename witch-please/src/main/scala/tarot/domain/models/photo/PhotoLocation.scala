package tarot.domain.models.photo

sealed trait PhotoLocation

object PhotoLocation:
  final case class Telegram(fileId: String) extends PhotoLocation
  final case class Local(path: String) extends PhotoLocation
  final case class S3(bucket: String, key: String) extends PhotoLocation
