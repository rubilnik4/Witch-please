package tarot.infrastructure.services.photo

import tarot.domain.models.TarotError
import tarot.domain.models.photo.PhotoFile
import zio.ZIO

trait TelegramFileService {
  def downloadPhoto(fileId: String): ZIO[Any, TarotError, PhotoFile]

  def sendPhoto(chatId: String, photo: PhotoFile): ZIO[Any, TarotError, String]
}
