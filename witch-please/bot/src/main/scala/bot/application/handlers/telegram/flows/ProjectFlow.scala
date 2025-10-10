package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.TelegramCommands
import bot.domain.models.session.BotPendingAction
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.tarot.authorize.AuthRequest
import shared.api.dto.tarot.projects.ProjectCreateRequest
import shared.api.dto.telegram.TelegramInlineKeyboardButton
import shared.infrastructure.services.telegram.TelegramApiService
import shared.models.tarot.authorize.ClientType
import zio.ZIO

object ProjectFlow {
  def getProjects(context: TelegramContext)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Get projects command for chat ${context.chatId}")

      session <- sessionService.get(context.chatId)
      userId <- ZIO.fromOption(session.userId)
        .orElseFail(new RuntimeException(s"UserId not found in session for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

      projects <- tarotApi.getProjects(userId, token)
      projectButtons = projects.zipWithIndex.map { case (project, idx) =>
        TelegramInlineKeyboardButton(s"${idx + 1}. ${project.name}", Some(TelegramCommands.spreadsGetCommand(project.id)))
      }
      createButton = TelegramInlineKeyboardButton("➕ Создать новую", Some(TelegramCommands.ProjectCreate))
      buttons = projectButtons :+ createButton
      _ <- telegramApi.sendInlineButtons(context.chatId, "Выбери сущность или создай новую", buttons)
    } yield ()
    
  def createProject(context: TelegramContext)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] = {
    for {
      _ <- ZIO.logInfo(s"Create project for chat ${context.chatId}")

      _ <- sessionService.setPending(context.chatId, BotPendingAction.ProjectName)
      _ <- telegramApi.sendReplyText(context.chatId, "Напиши название сущности")
    } yield ()
  }
  
  def setProjectName(context: TelegramContext, projectName: String)(
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
     
      _ <- telegramApi.sendText(context.chatId, s"Сущность $projectName создана")
      _ <- SpreadFlow.getSpreads(context, projectId)(telegramApi, tarotApi, sessionService)
    } yield ()
}
