package bot.application.handlers.telegram

import bot.application.commands.*
import bot.application.commands.telegram.TelegramCommands
import bot.domain.models.session.*
import bot.domain.models.telegram.*
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.tarot.authorize.*
import shared.api.dto.tarot.spreads.*
import shared.api.dto.tarot.users.*
import shared.api.dto.telegram.*
import shared.infrastructure.services.telegram.TelegramApiService
import shared.models.tarot.authorize.*
import zio.ZIO

import java.time.Instant
import java.util.UUID

object TelegramRouterHandler {
  def handle(context: TelegramContext, command: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      env <- ZIO.service[BotEnv]
      telegramApi = env.botService.telegramChannelService
      tarotApi = env.botService.tarotApiService
      sessionService = env.botService.botSessionService

      _ <- ZIO.logInfo(s"Received command from chat ${context.chatId}: $command")
      command <- ZIO.succeed(TelegramCommandParser.handle(command))
      _ <- command match {
        case BotCommand.Start =>
          handleStart(context)(telegramApi, tarotApi, sessionService)
        case BotCommand.GetProjects =>
          handleGetProjects(context)(telegramApi, tarotApi, sessionService)
        case BotCommand.CreateProject =>
          handleCreateProject(context)(telegramApi, tarotApi, sessionService)
        case BotCommand.GetSpreads(projectId: UUID) =>
          handleGetSpreads(context, projectId)(telegramApi, tarotApi, sessionService)
        case BotCommand.CreateSpread(title, count) =>
          handleCreateSpread(context)(telegramApi, sessionService)
        case BotCommand.CreateCard(description, index) =>
          handleCreateCard(context, description, index)(telegramApi, sessionService)
        case BotCommand.PublishSpread(at) =>
          handlePublishSpread(context, at)(telegramApi, tarotApi, sessionService)
        case BotCommand.Help =>
          telegramApi.sendText(context.chatId, helpText)
        case BotCommand.Unknown =>
          telegramApi.sendText(context.chatId, "Неизвестная команда. Введите /help.")
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

      _ <- telegramApi.sendText(context.chatId, s"Приветствую тебя $userName хозяйка таро!")
      _ <- handleGetProjects(context)(telegramApi, tarotApi, sessionService)
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
      projectButtons = projects.zipWithIndex.map { case (project, idx) =>
        TelegramInlineKeyboardButton(s"${idx + 1}. ${project.name}", Some(s"${TelegramCommands.SpreadsGet} ${project.id}"))
      }
      createButton = TelegramInlineKeyboardButton("➕ Создать новую", Some(TelegramCommands.ProjectCreate))
      buttons = projectButtons :+ createButton
      _ <- telegramApi.sendInlineButtons(context.chatId, "Выбери сущность или создай новую", buttons)
    } yield ()

  private def handleCreateProject(context: TelegramContext)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService) = {
    val message = "Напиши название сущности"
    createProject(context, message)(telegramApi, sessionService)
  }

  private def handleCreateSpread(context: TelegramContext)(
    telegramApi: TelegramApiService, sessionService: BotSessionService) =
    for {
      _ <- ZIO.logInfo(s"Create spread for chat ${context.chatId}")
      _ <- sessionService.setPending(context.chatId, BotPendingAction.SpreadTitle)
      _ <- telegramApi.sendReplyText(context.chatId, "Напиши название расклада")
    } yield ()

  private def handleGetSpreads(context: TelegramContext, projectId: UUID)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService) =
    for {
      _ <- ZIO.logInfo(s"GetProjects command: chat=${context.chatId}")
      session <- sessionService.get(context.chatId)
      projectId <- ZIO.fromOption(session.projectId)
        .orElseFail(new RuntimeException(s"ProjectId not found in session for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

      spreads <- tarotApi.getSpreads(projectId, token)
      cardButtons = spreads.zipWithIndex.map { case (spread, idx) =>
        TelegramInlineKeyboardButton(s"${idx + 1}. ${spread.title}", Some(s"${TelegramCommands.CardCreate}:${spread.id}"))
      }
      createButton = TelegramInlineKeyboardButton("➕ Создать новый", Some(TelegramCommands.SpreadCreate))
      buttons = cardButtons :+ createButton
      _ <- telegramApi.sendInlineButtons(context.chatId, "Выбери расклад или создай новый", buttons)
    } yield ()

  private def handleCreateCard(context: TelegramContext, description: String, index: Int)(
    telegramApi: TelegramApiService, sessionService: BotSessionService) =
    for {
      _ <- ZIO.logInfo(s"Create card for chat ${context.chatId}")
      _ <- sessionService.setPending(context.chatId, BotPendingAction.CardCover(description, index))
      _ <- telegramApi.sendText(context.chatId, s"Прикрепите фото для карты '$description'")
    } yield ()

  private def handlePublishSpread(context: TelegramContext, at: Instant)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService) =
    for {
      session <- sessionService.get(context.chatId)
      spreadId <- ZIO.fromOption(session.spreadId)
        .orElseFail(new RuntimeException(s"SpreadId not found for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found for chat ${context.chatId}"))

      _ <- ZIO.unless(session.spreadProgress.exists(p => p.createdCount == p.total)) {
        ZIO.fail(new RuntimeException("Нельзя опубликовать: не все карты загружены"))
      }

      _ <- ZIO.logInfo(s"Publish spread $spreadId for chat ${context.chatId}")
      _ <- tarotApi.publishSpread(SpreadPublishRequest(at), spreadId, token)
      _ <- telegramApi.sendText(context.chatId, s"Расклад $spreadId подтвержден")
      _ <- sessionService.clearSpread(context.chatId)
    } yield ()

  private def createProject(context: TelegramContext, text: String)(
    telegramApi: TelegramApiService, sessionService: BotSessionService) =
    for {
      _ <- ZIO.logInfo(s"Create project for chat ${context.chatId}")
      _ <- sessionService.setPending(context.chatId, BotPendingAction.ProjectName)
      _ <- telegramApi.sendReplyText(context.chatId, text)
    } yield ()

  private val helpText: String =
    """Команды:
      |/start
      |/help
      |""".stripMargin
}
