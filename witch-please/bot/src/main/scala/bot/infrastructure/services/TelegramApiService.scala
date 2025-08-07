package bot.infrastructure.services

import zio.Task

trait TelegramApiService {
  def sendText(chatId: Long, text: String): Task[Unit]
  def sendPhoto(chatId: Long, fileId: String): Task[Unit]
}
