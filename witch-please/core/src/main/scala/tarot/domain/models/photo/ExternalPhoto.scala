package tarot.domain.models.photo

sealed trait ExternalPhoto

object ExternalPhoto {
  final case class Telegram(fileId: String) extends ExternalPhoto

  def getFileId(photo: ExternalPhoto): Option[String] =
    photo match {
      case ExternalPhoto.Telegram(fileId) => Some(fileId)
    }
}