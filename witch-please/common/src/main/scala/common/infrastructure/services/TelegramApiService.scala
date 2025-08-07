package common.infrastructure.services

import common.models.telegram.{TelegramError, TelegramFile}
import zio.{Task, ZIO}

trait TelegramApiService {
  def sendText(chatId: Long, text: String): ZIO[Any, TelegramError, Long]
  def sendPhoto(chatId: Long, fileId: String): ZIO[Any, TelegramError, Unit]
  def downloadPhoto(fileId: String): ZIO[Any, TelegramError, TelegramFile]
  def sendPhoto(chatId: String, photo: TelegramFile): ZIO[Any, TelegramError, String]
}
