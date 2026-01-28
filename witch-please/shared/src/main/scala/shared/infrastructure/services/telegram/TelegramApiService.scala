package shared.infrastructure.services.telegram

import shared.api.dto.telegram.*
import shared.models.api.ApiError
import shared.models.telegram.*
import zio.ZIO

trait TelegramApiService {
  def sendText(chatId: Long, text: String): ZIO[Any, ApiError, Long]
  def sendReplyText(chatId: Long, text: String): ZIO[Any, ApiError, Long]
  def sendButton(chatId: Long, text: String, button: TelegramKeyboardButton): ZIO[Any, ApiError, Long]
  def sendButtons(chatId: Long, text: String, buttons: List[TelegramKeyboardButton]): ZIO[Any, ApiError, Long]
  def sendInlineButton(chatId: Long, text: String, button: TelegramInlineKeyboardButton): ZIO[Any, ApiError, Long]
  def sendInlineButtons(chatId: Long, text: String, buttons: List[TelegramInlineKeyboardButton]): ZIO[Any, ApiError, Long]
  def sendInlineGroupButtons(chatId: Long, text: String, buttons: List[List[TelegramInlineKeyboardButton]]): ZIO[Any, ApiError, Long]
  def sendPhoto(chatId: Long, fileId: String): ZIO[Any, ApiError, String]  
  def sendPhoto(chatId: Long, photo: TelegramFile): ZIO[Any, ApiError, String]
  def sendPhotos(chatId: Long, text: String, fileIds: List[String]): ZIO[Any, ApiError, Long]
  def downloadPhoto(fileId: String): ZIO[Any, ApiError, TelegramFile]
}
