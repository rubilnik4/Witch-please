package bot.application.handlers.telegram

import bot.application.commands.telegram.TelegramCommands
import bot.domain.models.session.*
import bot.domain.models.telegram.*
import bot.layers.*
import shared.api.dto.tarot.authorize.AuthRequest
import shared.api.dto.tarot.projects.ProjectCreateRequest
import shared.api.dto.tarot.spreads.*
import shared.api.dto.telegram.*
import shared.models.tarot.authorize.ClientType
import zio.*

object TelegramPendingHandler {
  def handleProjectName(context: TelegramContext, session: BotSession, projectName: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApiService <- ZIO.serviceWith[BotEnv](_.botService.telegramChannelService)
      botSessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)
      tarotApiService <- ZIO.serviceWith[BotEnv](_.botService.tarotApiService)

      _ <- ZIO.logInfo(s"Handle project name from chat ${context.chatId}")
      session <- botSessionService.get(context.chatId)
      userId <- ZIO.fromOption(session.userId)
        .orElseFail(new RuntimeException(s"UserId not found in session for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

      projectId <- tarotApiService.createProject(ProjectCreateRequest(projectName), token).map(_.id)
      authRequest = AuthRequest(ClientType.Telegram, userId, session.clientSecret, Some(projectId))
      authResponse <- tarotApiService.tokenAuth(authRequest)
      _ <- botSessionService.setProject(context.chatId, projectId, authResponse.token)

      button = TelegramInlineKeyboardButton("Создать расклад!", Some(TelegramCommands.SpreadCreate))
      _ <- telegramApiService.sendInlineButton(context.chatId, s"Создана сущность $projectId. Напиши название расклада", button)
    } yield ()

  def handleSpreadTitle(context: TelegramContext, session: BotSession, title: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApiService <- ZIO.serviceWith[BotEnv](_.botService.telegramChannelService)
      botSessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)
      tarotApiService <- ZIO.serviceWith[BotEnv](_.botService.tarotApiService)

      _ <- ZIO.logInfo(s"Handle spread title from chat ${context.chatId}")
      session <- botSessionService.get(context.chatId)

      _ <- botSessionService.setPending(context.chatId, BotPendingAction.SpreadCardCount(title))
      _ <- telegramApiService.sendReplyText(context.chatId, s"Укажи количество карт в раскладе")
    } yield ()

  def handleSpreadCardCount(context: TelegramContext, session: BotSession, title: String, cardCount: Int): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApiService <- ZIO.serviceWith[BotEnv](_.botService.telegramChannelService)
      botSessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)
      tarotApiService <- ZIO.serviceWith[BotEnv](_.botService.tarotApiService)

      _ <- ZIO.logInfo(s"Handle spread card count from chat ${context.chatId}")
      session <- botSessionService.get(context.chatId)

      _ <- botSessionService.setPending(context.chatId, BotPendingAction.SpreadCover(title, cardCount))
      _ <- telegramApiService.sendReplyText(context.chatId, s"Прикрепи фото для создания расклада")
    } yield ()

  def handleSpreadCover(context: TelegramContext, session: BotSession,
                        title: String, cardCount: Int, fileId: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApiService <- ZIO.serviceWith[BotEnv](_.botService.telegramChannelService)
      botSessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)
      tarotApiService <- ZIO.serviceWith[BotEnv](_.botService.tarotApiService)

      _ <- telegramApiService.sendText(context.chatId, s"Создаю расклад '$title'...")
      projectId <- ZIO.fromOption(session.projectId)
        .orElseFail(new RuntimeException(s"ProjectId not found in session for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

      _ <- ZIO.logDebug(s"Handle spread cover from chat ${context.chatId} for project $projectId")
      request = TelegramSpreadCreateRequest(projectId, title, cardCount, fileId)
      spreadId <- tarotApiService.createSpread(request, token).map(_.id)
      _ <- botSessionService.setSpread(context.chatId, spreadId, cardCount)
    } yield ()

  def handleCardCover(context: TelegramContext, session: BotSession,
                      description: String, index: Int, fileId: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApiService <- ZIO.serviceWith[BotEnv](_.botService.telegramChannelService)
      botSessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)
      tarotApiService <- ZIO.serviceWith[BotEnv](_.botService.tarotApiService)

      _ <- telegramApiService.sendText(context.chatId, s"Создаю карту '$description'...")
      spreadId <- ZIO.fromOption(session.spreadId)
        .orElseFail(new RuntimeException(s"SpreadId not found in session for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

      _ <- ZIO.logDebug(s"Handle card $index cover from chat ${context.chatId} for spread $spreadId")
      request = TelegramCardCreateRequest(description, fileId)
      _ <- tarotApiService.createCard(request, spreadId, index, token)
      _ <- botSessionService.setCard(context.chatId, index)
    } yield ()
}
