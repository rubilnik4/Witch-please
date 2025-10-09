package bot.application.handlers.telegram

import bot.application.commands.telegram.TelegramCommands
import bot.domain.models.session.*
import bot.domain.models.telegram.*
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.*
import shared.api.dto.tarot.authorize.AuthRequest
import shared.api.dto.tarot.projects.ProjectCreateRequest
import shared.api.dto.tarot.spreads.*
import shared.api.dto.telegram.*
import shared.infrastructure.services.telegram.TelegramApiService
import shared.models.tarot.authorize.ClientType
import zio.*

object TelegramPendingHandler {
  def handleProjectName(context: TelegramContext, projectName: String)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle project name from chat ${context.chatId}")
      
      session <- sessionService.get(context.chatId)
      userId <- ZIO.fromOption(session.userId)
        .orElseFail(new RuntimeException(s"UserId not found in session for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))     
      
      projectId <- tarotApi.createProject(ProjectCreateRequest(projectName), token).map(_.id)
      authRequest = AuthRequest(ClientType.Telegram, userId, session.clientSecret, Some(projectId))
      authResponse <- tarotApi.tokenAuth(authRequest)
      _ <- sessionService.setProject(context.chatId, projectId, authResponse.token)

      button = TelegramInlineKeyboardButton("Создать расклад!", Some(TelegramCommands.SpreadCreate))
      _ <- telegramApi.sendInlineButton(context.chatId, s"Создана сущность $projectId. Напиши название расклада", button)
    } yield ()

  def handleSpreadTitle(context: TelegramContext, title: String)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle spread title from chat ${context.chatId}")
      
      session <- sessionService.get(context.chatId)

      _ <- sessionService.setPending(context.chatId, BotPendingAction.SpreadCardCount(title))
      _ <- telegramApi.sendReplyText(context.chatId, s"Укажи количество карт в раскладе")
    } yield ()

  def handleSpreadCardCount(context: TelegramContext, title: String, cardCount: Int)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle spread card count from chat ${context.chatId}")
      
      session <- sessionService.get(context.chatId)

      _ <- sessionService.setPending(context.chatId, BotPendingAction.SpreadPhoto(title, cardCount))
      _ <- telegramApi.sendReplyText(context.chatId, s"Прикрепи фото для создания расклада")
    } yield ()

  def handleSpreadPhoto(context: TelegramContext, title: String, cardCount: Int, fileId: String)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle spread photo from chat ${context.chatId}")
      
      session <- sessionService.get(context.chatId)
      projectId <- ZIO.fromOption(session.projectId)
        .orElseFail(new RuntimeException(s"ProjectId not found in session for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))
      
      request = TelegramSpreadCreateRequest(projectId, title, cardCount, fileId)
      spreadId <- tarotApi.createSpread(request, token).map(_.id)
      _ <- sessionService.setSpread(context.chatId, spreadId, cardCount)
    } yield ()

  def handleCardIndex(context: TelegramContext, index: Int)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle card index $index from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)

      _ <- sessionService.setPending(context.chatId, BotPendingAction.CardDescription(index))
      _ <- telegramApi.sendReplyText(context.chatId, s"Напиши описание карты")
    } yield ()

  def handleCardDescription(context: TelegramContext, index: Int, description: String)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle card description from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)

      _ <- sessionService.setPending(context.chatId, BotPendingAction.CardPhoto(index, description))
      _ <- telegramApi.sendReplyText(context.chatId, s"Прикрепи фото для создания карты")
    } yield ()
    
  def handleCardPhoto(context: TelegramContext, index: Int, description: String, fileId: String)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle card photo from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)
      _ <- telegramApi.sendText(context.chatId, s"Создаю карту '$description'...")
      spreadId <- ZIO.fromOption(session.spreadId)
        .orElseFail(new RuntimeException(s"SpreadId not found in session for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

      request = TelegramCardCreateRequest(description, fileId)
      _ <- tarotApi.createCard(request, spreadId, index, token)
      _ <- sessionService.setCard(context.chatId, index)
    } yield ()
}
