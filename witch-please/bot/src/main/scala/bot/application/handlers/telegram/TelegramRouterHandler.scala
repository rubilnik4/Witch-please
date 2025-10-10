package bot.application.handlers.telegram

import bot.application.commands.*
import bot.application.commands.telegram.TelegramCommands
import bot.application.handlers.telegram.flows.*
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
      telegramApi <- ZIO.serviceWith[BotEnv](_.botService.telegramApiService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.botService.tarotApiService)
      sessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)

      _ <- ZIO.logInfo(s"Received command from chat ${context.chatId}: $command")
      command <- ZIO.succeed(TelegramCommandParser.handle(command))
      _ <- command match {
        case BotCommand.Start =>
          handleStart(context)(telegramApi, tarotApi, sessionService)
        case BotCommand.GetProjects =>
          ProjectFlow.getProjects(context)(telegramApi, tarotApi, sessionService)
        case BotCommand.CreateProject =>
          ProjectFlow.createProject(context)(telegramApi, tarotApi, sessionService)
        case BotCommand.GetSpreads(projectId: UUID) =>
          SpreadFlow.getSpreads(context, projectId)(telegramApi, tarotApi, sessionService)
        case BotCommand.CreateSpread =>
          SpreadFlow.createSpread(context)(telegramApi, sessionService)
        case BotCommand.CreateCard =>
          CardFlow.createCard(context)(telegramApi, sessionService)
        case BotCommand.GetCards(spreadId: UUID) =>
          CardFlow.getCards(context, spreadId)(telegramApi, tarotApi, sessionService)
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
      _ <- ZIO.logInfo(s"Start command for chat ${context.chatId}")

      userName <- ZIO.fromOption(context.username)
        .orElseFail(new RuntimeException(s"UserId not found in session for chat ${context.username}"))
      session <- sessionService.start(context.chatId, userName)

      userRequest = UserCreateRequest(context.clientId.toString, session.clientSecret, userName)
      userId <- tarotApi.getOrCreateUserId(userRequest)
      authRequest = AuthRequest(ClientType.Telegram, userId, session.clientSecret, None)
      token <- tarotApi.tokenAuth(authRequest).map(_.token)
      _ <- sessionService.setUser(context.chatId, userId, token)

      _ <- telegramApi.sendText(context.chatId, s"Приветствую тебя $userName хозяйка таро!")
      _ <- ProjectFlow.getProjects(context)(telegramApi, tarotApi, sessionService)
    } yield () 

  private def handlePublishSpread(context: TelegramContext, publishAt: Instant)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService) =
    for {
      session <- sessionService.get(context.chatId)
      spreadId <- ZIO.fromOption(session.spreadId)
        .orElseFail(new RuntimeException(s"SpreadId not found for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found for chat ${context.chatId}"))

      _ <- ZIO.logInfo(s"Publish spread $spreadId for chat ${context.chatId}")

      _ <- ZIO.unless(session.spreadProgress.exists(p => p.createdCount == p.cardsCount)) {
        telegramApi.sendText(context.chatId, s"Нельзя опубликовать: не все карты загружены") *>
          ZIO.logError("Can't publish. Not all cards uploaded") *>
            ZIO.fail(new RuntimeException("Can't publish. Not all cards uploaded"))
      }

      _ <- tarotApi.publishSpread(SpreadPublishRequest(publishAt), spreadId, token)
      _ <- telegramApi.sendText(context.chatId, s"Расклад $spreadId подтвержден")
      _ <- sessionService.clearSpread(context.chatId)
    } yield ()

  private val helpText: String =
    """Команды:
      |/start
      |/help
      |""".stripMargin
}
