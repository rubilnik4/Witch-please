package bot.application.handlers.telegram

import bot.domain.models.telegram.*
import bot.layers.BotEnv
import zio.*

object TelegramTextHandler {
  def handle(context: TelegramContext, text: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Ignored plain text from ${context.chatId}: $text")
      telegramApiService <- ZIO.serviceWith[BotEnv](_.botService.telegramApiService)
      _ <- telegramApiService.sendText(context.chatId, "Пожалуйста, используйте команды. Введите /help.")
    } yield ()
}
