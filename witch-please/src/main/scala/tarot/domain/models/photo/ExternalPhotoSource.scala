package tarot.domain.models.photo

sealed trait ExternalPhotoSource

object ExternalPhotoSource:
  final case class Telegram(fileId: String) extends ExternalPhotoSource
