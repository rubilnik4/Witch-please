package bot.application.handlers

import bot.api.dto.TelegramWebhookRequest
import bot.application.commands.BotCommand
import bot.domain.models.session.BotSession
import bot.domain.models.telegram.*
import bot.infrastructure.services.authorize.SecretService
import bot.layers.AppEnv
import shared.api.dto.tarot.projects.*
import shared.api.dto.tarot.spreads.*
import shared.api.dto.tarot.users.*
import zio.ZIO

final class TelegramCommandHandlerLive extends TelegramCommandHandler {
  def handle(message: TelegramMessage): ZIO[AppEnv, Throwable, Unit] =
    message match {
      case TelegramMessage.Text(context, text) =>
        handleText(context, text)
      case TelegramMessage.Photo(context, fileId) =>
        handlePhoto(context, fileId)
      case TelegramMessage.Command(context, cmd) =>
        handleCommand(context, cmd)
      case _ =>
        ZIO.logError("Unsupported telegram request type received")
    }

  private def handleText(context: TelegramContext, text: String): ZIO[AppEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Ignored plain text from ${context.chatId}: $text")
      telegramApiService <- ZIO.serviceWith[AppEnv](_.botService.telegramApiService)
      _ <- telegramApiService.sendText(context.chatId, "Пожалуйста, используйте команды. Введите /help.")
    } yield ()

  private def handlePhoto(context: TelegramContext, fileId: String): ZIO[AppEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Received photo from chat ${context.chatId} (fileId: $fileId)")
      telegramApiService <- ZIO.serviceWith[AppEnv](_.botService.telegramApiService)
      _ <- telegramApiService.sendPhoto(context.chatId, fileId)
    } yield ()

  private def handleCommand(context: TelegramContext, command: String): ZIO[AppEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Received command from chat ${context.chatId}: $command")
      telegramApiService <- ZIO.serviceWith[AppEnv](_.botService.telegramApiService)
      tarotApiService <- ZIO.serviceWith[AppEnv](_.botService.tarotApiService)
      botSessionService <- ZIO.serviceWith[AppEnv](_.botService.botSessionService)

      _ <- BotCommandHandler.handle(command) match {
        case BotCommand.Start =>
          for {
            _ <- telegramApiService.sendText(context.chatId, "Привет! Это таро бот. Узнай как пройдет твой день")
            - <- botSessionService.start(context.chatId)
          } yield ()
        case BotCommand.CreateUser(name) =>
          for {
            _ <- telegramApiService.sendText(chatId, s"Создаю пользователя '$name'")
            request = UserCreateRequest(name)
            userId <- tarotApiService.createUser(request)
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
