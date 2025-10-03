package bot.application.handlers.telegram

import bot.application.commands.*
import bot.application.commands.telegram.*
import bot.domain.models.session.*
import bot.domain.models.telegram.*
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.tarot.authorize.*
import shared.api.dto.tarot.projects.*
import shared.api.dto.tarot.spreads.*
import shared.api.dto.tarot.users.*
import shared.api.dto.telegram.*
import shared.infrastructure.services.telegram.TelegramApiService
import shared.models.tarot.authorize.*
import zio.ZIO

import java.time.Instant
import java.util.UUID

object TelegramRouterHandler {
  def handle(ctx: TelegramContext, command: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      env <- ZIO.service[BotEnv]
      telegramApi = env.botService.telegramChannelService
      tarotApi = env.botService.tarotApiService
      sessionService = env.botService.botSessionService

      _ <- ZIO.logInfo(s"Received command from chat ${ctx.chatId}: $command")
      command <- ZIO.succeed(TelegramCommandParser.handle(command))
      _ <- command match {
        case BotCommand.Start =>
          handleStart(ctx)(telegramApi, tarotApi, sessionService)
        case BotCommand.GetProjects =>
          handleGetProjects(ctx)(telegramApi, tarotApi, sessionService)
        case BotCommand.CreateProject(name) =>
          handleCreateProject(ctx, name)(telegramApi, tarotApi, sessionService)
        case BotCommand.CreateSpread(title, count) =>
          handleCreateSpread(ctx, title, count)(telegramApi, sessionService)
        case BotCommand.CreateCard(description, index) =>
          handleCreateCard(ctx, description, index)(telegramApi, sessionService)
        case BotCommand.PublishSpread(at) =>
          handlePublishSpread(ctx, at)(telegramApi, tarotApi, sessionService)
        case BotCommand.Help =>
          telegramApi.sendText(ctx.chatId, helpText)
        case BotCommand.Unknown =>
          telegramApi.sendText(ctx.chatId, "Неизвестная команда. Введите /help.")
      }
    } yield ()

  private def handleStart(context: TelegramContext)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService) =
    for {
      _ <- ZIO.logInfo(s"Start command: chat=${context.chatId}, clientId=${context.clientId}")
      userName <- ZIO.fromOption(context.username)
        .orElseFail(new RuntimeException(s"UserId not found in session for chat ${context.username}"))
      session <- sessionService.start(context.chatId, userName)

      userRequest = UserCreateRequest(context.clientId.toString, session.clientSecret, userName)
      userId <- tarotApi.getOrCreateUserId(userRequest)
      authRequest = AuthRequest(ClientType.Telegram, userId, session.clientSecret, None)
      token <- tarotApi.tokenAuth(authRequest).map(_.token)
      _ <- sessionService.setUser(context.chatId, userId, token)

      buttons = List(
        TelegramKeyboardButton("Создать сущность!", Some(TelegramCommands.ProjectCreate)),
        TelegramKeyboardButton("Просмотреть сущности!", Some(TelegramCommands.ProjectsGet))
      )
      _ <- telegramApi.sendButtons(context.chatId, s"Приветствую тебя $userName хозяйка таро. Приказывай!", buttons)
    } yield ()

  private def handleGetProjects(context: TelegramContext)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService) =
    for {
      _ <- ZIO.logInfo(s"GetProjects command: chat=${context.chatId}")
      session <- sessionService.get(context.chatId)
      userId <- ZIO.fromOption(session.userId)
        .orElseFail(new RuntimeException(s"UserId not found in session for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

      projects <- tarotApi.getProjects(userId, token)
      buttons = projects.map(project => TelegramKeyboardButton(project.name))
      _ <- telegramApi.sendButtons(context.chatId, "Выбери сущность", buttons)
    } yield ()

  private def handleCreateProject(context: TelegramContext, projectName: String)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService) =
    for {
      _ <- ZIO.logInfo(s"CreateProject: chat=${context.chatId}, name='$projectName'")
      session <- sessionService.get(context.chatId)
      userId <- ZIO.fromOption(session.userId)
        .orElseFail(new RuntimeException(s"UserId not found in session for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

      projectId <- tarotApi.createProject(ProjectCreateRequest(projectName), token).map(_.id)
      authRequest = AuthRequest(ClientType.Telegram, userId, session.clientSecret, Some(projectId))
      authResponse <- tarotApi.tokenAuth(authRequest)
      _ <- sessionService.setProject(context.chatId, projectId, authResponse.token)

      button = TelegramKeyboardButton("Создать расклад!", Some(TelegramCommands.SpreadCreate))
      _ <- telegramApi.sendButton(context.chatId, s"Создана сущность $projectId. Сделай расклад?", button)
    } yield ()

  private def handleCreateSpread(ctx: TelegramContext, title: String, count: Int)(
    telegramApi: TelegramApiService, sessionService: BotSessionService) =
    for {
      _ <- sessionService.setPending(ctx.chatId, BotPendingAction.SpreadCover(title, count))
      _ <- telegramApi.sendText(ctx.chatId, s"Прикрепи фото для создания расклада '$title'")
    } yield ()

  private def handleCreateCard(ctx: TelegramContext, description: String, index: Int)(
    telegramApi: TelegramApiService, sessionService: BotSessionService) =
    for {
      _ <- sessionService.setPending(ctx.chatId, BotPendingAction.CardCover(description, index))
      _ <- telegramApi.sendText(ctx.chatId, s"Прикрепите фото для карты '$description'")
    } yield ()

  private def handlePublishSpread(ctx: TelegramContext, at: Instant)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService) =
    for {
      session <- sessionService.get(ctx.chatId)
      spreadId <- ZIO.fromOption(session.spreadId)
        .orElseFail(new RuntimeException(s"SpreadId not found for chat ${ctx.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found for chat ${ctx.chatId}"))

      _ <- ZIO.unless(session.spreadProgress.exists(p => p.createdCount == p.total)) {
        ZIO.fail(new RuntimeException("Нельзя опубликовать: не все карты загружены"))
      }

      _ <- tarotApi.publishSpread(SpreadPublishRequest(at), spreadId, token)
      _ <- telegramApi.sendText(ctx.chatId, s"Расклад $spreadId подтвержден")
      _ <- sessionService.clearSpread(ctx.chatId)
    } yield ()

  private val helpText: String =
    """Команды:
      |/start
      |/help
      |""".stripMargin
}
