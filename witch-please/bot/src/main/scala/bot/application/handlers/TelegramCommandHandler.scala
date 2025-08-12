package bot.application.handlers

import bot.api.dto.TelegramWebhookRequest
import bot.application.commands.BotCommand
import bot.domain.models.telegram.TelegramMessage
import bot.layers.AppEnv
import shared.api.dto.tarot.projects.*
import shared.api.dto.tarot.spreads.*
import shared.api.dto.tarot.users.*
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
      telegramApiService <- ZIO.serviceWith[AppEnv](_.botService.telegramApiService)
      _ <- telegramApiService.sendText(chatId, "Пожалуйста, используйте команды. Введите /help.")
    } yield ()

  private def handlePhoto(chatId: Long, fileId: String): ZIO[AppEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Received photo from chat $chatId (fileId: $fileId)")
      telegramApiService <- ZIO.serviceWith[AppEnv](_.botService.telegramApiService)
      _ <- telegramApiService.sendPhoto(chatId, fileId)
    } yield ()

  private def handleCommand(chatId: Long, command: String): ZIO[AppEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Received command from chat $chatId: $command")
      telegramApiService <- ZIO.serviceWith[AppEnv](_.botService.telegramApiService)
      tarotApiService <- ZIO.serviceWith[AppEnv](_.botService.tarotApiService)

      _ <- BotCommandHandler.handle(command) match {
        case BotCommand.Start =>
          telegramApiService.sendText(chatId, "Привет! Это таро бот. Узнай как пройдет твой день")
        case BotCommand.CreateUser(name) =>
          for {
            _ <- telegramApiService.sendText(chatId, s"Создаю пользователя '$name'")
            request = UserCreateRequest(name)
            userId <- tarotApiService.createUser(name)
          } yield ()
        case BotCommand.CreateProject(name) =>
          for {
            _ <- telegramApiService.sendText(chatId, s"Создаю проект '$name'")
            request = ProjectCreateRequest(name)
            projectId <- tarotApiService.createProject(name)
          } yield ()
        case BotCommand.CreateSpread(name) =>
          for {
            _ <- telegramApiService.sendText(chatId, s"Создаю расклад '$name'...")
            request = TelegramSpreadCreateRequest(name)
            projectId <- tarotApiService.createSpread(name)
          } yield ()
        case BotCommand.CreateCard(index, name) =>
          telegramApiService.sendText(chatId, s"Создаю карту '$name'...")
        case BotCommand.ConfirmSpread(spreadId) =>
          telegramApiService.sendText(chatId, s"Расклад $spreadId подтверждён.")
        case BotCommand.Help =>
          telegramApiService.sendText(chatId, "Команды:\n/start\n/help\n/project_create <имя>\n/spread_confirm <id>")
        case BotCommand.Unknown =>
          telegramApiService.sendText(chatId, "Неизвестная команда. Введите /help.")
      }
    } yield ()
}
