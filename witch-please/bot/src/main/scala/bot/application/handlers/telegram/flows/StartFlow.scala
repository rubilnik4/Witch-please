package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.TelegramCommands
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.tarot.authorize.AuthRequest
import shared.api.dto.tarot.users.UserCreateRequest
import shared.api.dto.telegram.TelegramInlineKeyboardButton
import shared.infrastructure.services.telegram.TelegramApiService
import shared.models.api.ApiError
import shared.models.tarot.authorize.ClientType
import zio.ZIO

object StartFlow {
  def handleStart(context: TelegramContext)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[Any, ApiError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Start command for chat ${context.chatId}")

      userButton = TelegramInlineKeyboardButton("Хочу предсказаний", Some(TelegramCommands.ClientStart))
      adminButton = TelegramInlineKeyboardButton("Я тарологичка", Some(TelegramCommands.AuthorStart))
      buttons = List(userButton, adminButton)
      _ <- telegramApi.sendInlineButtons(context.chatId, "Кто внутри тебя?", buttons)
    } yield ()

  def handleAuthorStart(context: TelegramContext)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Start as admin command for chat ${context.chatId}")

      username <- ZIO.fromOption(context.username)
        .orElseFail(new RuntimeException(s"UserId not found in context for chat ${context.username}"))
      token <- setUser(context, username)(telegramApi, tarotApi, sessionService)     

      spreads <- tarotApi.getSpreads(token)
      _ <- SpreadFlow.showSpreads(context, spreads)(telegramApi, tarotApi, sessionService)
    } yield ()

  def handleClientStart(context: TelegramContext)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Start as user command for chat ${context.chatId}")

      username <- ZIO.fromOption(context.username)
        .orElseFail(new RuntimeException(s"UserId not found in context for chat ${context.username}"))    

      _ <- telegramApi.sendText(context.chatId, s"Приветствую тебя $username страждущий!")
      _ <- ClientFlow.showAuthors(context)(telegramApi, tarotApi, sessionService)
    } yield ()

  private def setUser(context: TelegramContext, username: String)
    (telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService) =
    for {
      session <- sessionService.start(context.chatId, username)

      userRequest = UserCreateRequest(context.clientId.toString, session.clientSecret, username)
      userId <- tarotApi.getOrCreateUserId(userRequest)
      authRequest = AuthRequest(ClientType.Telegram, userId, session.clientSecret)
      token <- tarotApi.tokenAuth(authRequest).map(_.token)

      _ <- sessionService.setUser(context.chatId, userId, token)
      _ <- telegramApi.sendText(context.chatId, s"Приветствую тебя $username хозяйка таро!")
    }  yield token
}
