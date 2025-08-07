package bot.application.handlers

import bot.api.dto.TelegramWebhookRequest
import bot.domain.models.TelegramMessage
import bot.layers.AppEnv
import zio.ZIO

object TelegramCommandHandler {
  def handle(message: TelegramMessage): ZIO[AppEnv, Throwable, Unit] =
    message match {
      case TelegramMessage.Text(chatId, text) =>
        handleText(chatId, text)
      case TelegramMessage.Photo(chatId, fileId) =>
        handlePhoto(chatId, fileId)
      case TelegramMessage.Command(chatId, cmd) =>
        handleCommand(chatId, cmd)
      case _ =>
        ZIO.logError("Unsupported telegram request type received")
    }

  private def handleText(chatId: Long, text: String): ZIO[AppEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Ignored plain text from $chatId: $text")
      _ <- TelegramMessageSender.sendText(chatId, "Пожалуйста, используйте команды. Введите /help.")
    } yield ()

  private def handlePhoto(chatId: Long, fileId: String): ZIO[AppEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Получено фото от $chatId, fileId: $fileId")
      _ <- TelegramMessageSender.sendText(chatId, "Фото получено. Обрабатываем...")
      // Загрузить и сохранить фото — пока можно отложить
    } yield ()

  private def handleCommand(chatId: Long, command: String): ZIO[AppEnv, Throwable, Unit] =
    BotCommandParser.parse(command) match {
      case BotCommand.Start =>
        TelegramMessageSender.sendText(chatId, "Привет! Это бот.")

      case BotCommand.CreateProject(name) =>
        TelegramMessageSender.sendText(chatId, s"Создаю проект '$name'...")

      case BotCommand.ConfirmSpread(spreadId) =>
        TelegramMessageSender.sendText(chatId, s"Расклад $spreadId подтверждён.")

      case BotCommand.Help =>
        TelegramMessageSender.sendText(chatId, "Команды:\n/start\n/help\n/project_create <имя>\n/spread_confirm <id>")

      case BotCommand.Unknown =>
        TelegramMessageSender.sendText(chatId, "Неизвестная команда. Введите /help.")
    }
}
