package bot.application.handlers.telegram

import bot.application.commands.BotCommand
import bot.application.commands.telegram.TelegramCommands
import bot.domain.models.session.*
import bot.domain.models.telegram.*
import bot.layers.BotEnv
import shared.api.dto.tarot.authorize.AuthRequest
import shared.api.dto.tarot.projects.*
import shared.api.dto.tarot.spreads.*
import shared.api.dto.tarot.users.*
import shared.api.dto.telegram.TelegramKeyboardButton
import shared.models.tarot.authorize.ClientType
import zio.ZIO

import java.time.Instant

final class TelegramCommandHandlerLive extends TelegramCommandHandler {
  def handle(message: TelegramMessage): ZIO[BotEnv, Throwable, Unit] =
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

  private def handleText(context: TelegramContext, text: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Ignored plain text from ${context.chatId}: $text")
      telegramApiService <- ZIO.serviceWith[BotEnv](_.botService.telegramApiService)
      _ <- telegramApiService.sendText(context.chatId, "Пожалуйста, используйте команды. Введите /help.")
    } yield ()

  private def handlePhoto(context: TelegramContext, fileId: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApiService <- ZIO.serviceWith[BotEnv](_.botService.telegramApiService)
      tarotApiService <- ZIO.serviceWith[BotEnv](_.botService.tarotApiService)
      botSessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)

      session <- botSessionService.get(context.chatId)
      _ <- ZIO.logInfo(s"Received photo (fileId: $fileId) from chat ${context.chatId} for pending action ${session.pending}")
      _ <- session.pending match {
        case Some(BotPendingAction.SpreadCover(title, cardCount)) =>
          for {
            _ <- telegramApiService.sendText(context.chatId, s"Создаю расклад '$title'...")
            session <- botSessionService.get(context.chatId)
            projectId <- ZIO.fromOption(session.projectId)
              .orElseFail(new RuntimeException(s"ProjectId not found in session for chat ${context.chatId}"))
            token <- ZIO.fromOption(session.token)
              .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

            request = TelegramSpreadCreateRequest(projectId, title, cardCount, fileId)
            spreadId <- tarotApiService.createSpread(request, token).map(_.id)
            _ <- botSessionService.setSpread(context.chatId, spreadId, cardCount)
          } yield ()
        case Some(BotPendingAction.CardCover(description, index)) =>
          for {
            _ <- telegramApiService.sendText(context.chatId, s"Создаю карту '$description'...")
            session <- botSessionService.get(context.chatId)
            spreadId <- ZIO.fromOption(session.spreadId)
              .orElseFail(new RuntimeException(s"SpreadId not found in session for chat ${context.chatId}"))
            token <- ZIO.fromOption(session.token)
              .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

            request = TelegramCardCreateRequest(description, fileId)
            _ <- tarotApiService.createCard(request, spreadId, index, token)
            _ <- botSessionService.setCard(context.chatId, index)

          } yield ()
        case None =>
          for {
            _ <- ZIO.logError(s"Unknown photo pending action ${session.pending} from chat ${context.chatId}")
            _ <- telegramApiService.sendText(context.chatId, "Неизвестная команда отправки фото.")
          } yield ()
      }
    } yield ()

  private def handleCommand(context: TelegramContext, command: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Received command from chat ${context.chatId}: $command")
      telegramApiService <- ZIO.serviceWith[BotEnv](_.botService.telegramApiService)
      tarotApiService <- ZIO.serviceWith[BotEnv](_.botService.tarotApiService)
      botSessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)

      _ <- TelegramCommandParser.handle(command) match {
        case BotCommand.Start =>          
          for {
            userName <- ZIO.fromOption(context.username)
              .orElseFail(new RuntimeException(s"UserId not found in session for chat ${context.username}"))           
            session <- botSessionService.start(context.chatId)   

            userRequest = UserCreateRequest(context.clientId.toString, session.clientSecret, userName)
            userId <- tarotApiService.getOrCreateUserId(userRequest)
            authRequest = AuthRequest(ClientType.Telegram, userId, session.clientSecret, None)
            token <- tarotApiService.tokenAuth(authRequest).map(_.token)
            _ <- botSessionService.setUser(context.chatId, userId, token)

            button = TelegramKeyboardButton("Создать сущность!", Some(TelegramCommands.ProjectCreate))
            _ <- telegramApiService.sendButton(context.chatId, s"Приветствую тебя $userName хозяйка таро. Приказывай!", button)
          } yield ()
        case BotCommand.CreateProject(name) =>
          for {
            session <- botSessionService.get(context.chatId)
            userId <- ZIO.fromOption(session.userId)
              .orElseFail(new RuntimeException(s"UserId not found in session for chat ${context.chatId}"))
            token <- ZIO.fromOption(session.token)
              .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

            request = ProjectCreateRequest(name)
            projectId <- tarotApiService.createProject(request, token).map(_.id)
            authRequest = AuthRequest(ClientType.Telegram, userId, session.clientSecret, Some(projectId))
            authResponse <- tarotApiService.tokenAuth(authRequest)
            _ <- botSessionService.setProject(context.chatId, projectId, token)

            button = TelegramKeyboardButton("Создать расклад!", Some(TelegramCommands.SpreadCreate))
            _ <- telegramApiService.sendButton(context.chatId, s"Нашли сущность $projectId. Что дальше?", button)
          } yield ()
        case BotCommand.CreateSpread(title: String, cardCount: Int) =>
          val pending = BotPendingAction.SpreadCover(title, cardCount)
          for {
            _ <- botSessionService.setPending(context.chatId, pending)
            _ <- telegramApiService.sendText(context.chatId, s"Прикрепи фото для создания расклада $title")
          } yield ()
        case BotCommand.CreateCard(description, index) =>
          val pending = BotPendingAction.CardCover(description, index)
          for {
            _ <- botSessionService.setPending(context.chatId, pending)
            _ <- telegramApiService.sendText(context.chatId, s"Прикрепите фото для создания карты $description")
          } yield ()
        case BotCommand.PublishSpread(scheduledAt: Instant) =>
          for {
            session <- botSessionService.get(context.chatId)
            spreadId <- ZIO.fromOption(session.spreadId)
              .orElseFail(new RuntimeException(s"SpreadId not found in session for chat ${context.chatId}"))
            token <- ZIO.fromOption(session.token)
              .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

            _ <- ZIO.unless(session.spreadProgress.exists(progress => progress.createdCount == progress.total)) {
              ZIO.fail(new RuntimeException("Cannot publish: not all cards uploaded"))
            }

            request = SpreadPublishRequest(scheduledAt)
            _ <- tarotApiService.publishSpread(request, spreadId, token)
            _ <- telegramApiService.sendText(context.chatId, s"Расклад $spreadId подтвержден")
            _ <- botSessionService.clearSpread(context.chatId)
          } yield ()
        case BotCommand.Help =>
          telegramApiService.sendText(context.chatId, "Команды:\n/start\n/help\n/project_create <имя>\n/spread_confirm <id>")
        case BotCommand.Unknown =>
          telegramApiService.sendText(context.chatId, "Неизвестная команда. Введите /help.")
      }
    } yield ()
}
