package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.*
import bot.domain.models.telegram.TelegramContext
import bot.layers.BotEnv
import shared.api.dto.tarot.authorize.AuthRequest
import shared.api.dto.tarot.users.UserCreateRequest
import shared.api.dto.telegram.TelegramInlineKeyboardButton
import shared.models.tarot.authorize.ClientType
import zio.ZIO

object StartFlow {
  def handleStart(context: TelegramContext): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Start command for chat ${context.chatId}")

      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      
      userButton = TelegramInlineKeyboardButton("Хочу предсказаний", Some(ClientCommands.Start))
      adminButton = TelegramInlineKeyboardButton("Я тарологичка", Some(AuthorCommands.Start))
      buttons = List(userButton, adminButton)
      _ <- telegramApi.sendInlineButtons(context.chatId, "Кто внутри тебя?", buttons)
    } yield ()

  def handleAuthorStart(context: TelegramContext): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Start as admin command for chat ${context.chatId}")

      username <- getUsername(context)
      token <- setUser(context, username)
      _ <- ChannelFlow.selectChannel(context)
    } yield ()

  def handleClientStart(context: TelegramContext): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Start as user command for chat ${context.chatId}")

      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      
      username <- getUsername(context)
      _ <- telegramApi.sendText(context.chatId, s"Приветствую тебя $username страждущий!")
      _ <- ClientFlow.showAuthors(context)
    } yield ()

  private def setUser(context: TelegramContext, username: String) =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      session <- sessionService.start(context.chatId, username)

      userRequest = UserCreateRequest(context.clientId.toString, session.clientSecret, username)
      userId <- tarotApi.getOrCreateUserId(userRequest)
      authRequest = AuthRequest(ClientType.Telegram, userId.id, session.clientSecret)
      token <- tarotApi.tokenAuth(authRequest).map(_.token)

      _ <- sessionService.setUser(context.chatId, userId.id, token)
      _ <- telegramApi.sendText(context.chatId, s"Приветствую тебя $username хозяйка таро!")
    }  yield token

  private def getUsername(context: TelegramContext) =
    ZIO.fromOption(context.username)
      .orElseFail(new RuntimeException(s"UserId not found in context for chat ${context.username}"))
}
