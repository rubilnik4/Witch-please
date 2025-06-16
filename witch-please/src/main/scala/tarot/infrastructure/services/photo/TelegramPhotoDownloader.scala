package tarot.infrastructure.services.photo

import tarot.domain.models.TarotError
import tarot.domain.models.photo.PhotoFile
import zio.ZIO

trait TelegramPhotoDownloader {
  def download(fileId: String): ZIO[Any, TarotError, PhotoFile]
}
