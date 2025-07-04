package tarot.domain.models.photo

sealed trait ExternalPhoto

object ExternalPhoto:
  final case class Telegram(fileId: String) extends ExternalPhoto
