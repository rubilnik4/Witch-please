package shared.infrastructure.services.telegram

import shared.api.dto.telegram.TelegramKeyboardButton
import shared.models.api.ApiError
import shared.models.telegram.*
import zio.{Task, ZIO}

trait TelegramApiService {
  def sendText(chatId: Long, text: String): ZIO[Any, ApiError, Long]
  def sendButton(chatId: Long, text: String, button: TelegramKeyboardButton): ZIO[Any, ApiError, Long]
  def sendPhoto(chatId: Long, fileId: String): ZIO[Any, ApiError, String]
  def sendPhoto(chatId: Long, photo: TelegramFile): ZIO[Any, ApiError, String]
  def downloadPhoto(fileId: String): ZIO[Any, ApiError, TelegramFile]
}
