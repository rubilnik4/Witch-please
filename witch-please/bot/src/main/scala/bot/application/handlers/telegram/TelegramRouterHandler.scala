package bot.application.handlers.telegram

import bot.application.commands.*
import bot.application.handlers.telegram.flows.*
import bot.domain.models.session.*
import bot.domain.models.telegram.*
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.tarot.authorize.*
import shared.api.dto.tarot.users.*
import shared.infrastructure.services.telegram.TelegramApiService
import shared.models.tarot.authorize.*
import zio.ZIO

import java.util.UUID

object TelegramRouterHandler {
  def handle(context: TelegramContext, command: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.botService.telegramApiService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.botService.tarotApiService)
      sessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)

      _ <- ZIO.logInfo(s"Received command from chat ${context.chatId}: $command")
      _ <- TelegramCommandParser.handle(command) match {
        case tarotCommand: TarotCommand =>
          handleTarotCommand(context, tarotCommand)(telegramApi, tarotApi, sessionService)        
        case scheduleCommand: ScheduleCommand =>
          SchedulerFlow.handle(context, scheduleCommand)(telegramApi, tarotApi, sessionService)
        case BotCommand.Noop =>
          ZIO.logInfo(s"Empty noop command for chat ${context.chatId}")
        case BotCommand.Help =>
          telegramApi.sendText(context.chatId, helpText)
        case BotCommand.Unknown =>
          telegramApi.sendText(context.chatId, "Неизвестная команда. Введите /help.")  
      }
    } yield ()

  private def handleTarotCommand(context: TelegramContext, command: TarotCommand)
    (telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
      for {
        _ <- command match {
          case TarotCommand.Start =>
            handleStart(context)(telegramApi, tarotApi, sessionService)       
          case TarotCommand.CreateProject =>
            ProjectFlow.createProject(context)(telegramApi, tarotApi, sessionService)
          case TarotCommand.SelectProject(projectId: UUID) =>
            ProjectFlow.selectProject(context, projectId)(telegramApi, tarotApi, sessionService)        
          case TarotCommand.CreateSpread =>
            SpreadFlow.createSpread(context)(telegramApi, sessionService)
          case TarotCommand.SelectSpread(spreadId: UUID, cardCount: Int) =>
            SpreadFlow.selectSpread(context, spreadId, cardCount)(telegramApi, tarotApi, sessionService)
          case TarotCommand.SelectSpreadCards(spreadId: UUID) =>
            CardFlow.selectSpreadCards(context, spreadId)(telegramApi, tarotApi, sessionService)
          case TarotCommand.PublishSpread(spreadId: UUID) =>
            PublishFlow.publishSpread(context)(telegramApi, tarotApi, sessionService)
          case TarotCommand.DeleteSpread(spreadId: UUID) =>
            SpreadFlow.deleteSpread(context)(telegramApi, tarotApi, sessionService)
          case TarotCommand.CreateCard(index: Int) =>
            CardFlow.createCard(context, index)(telegramApi, sessionService)
       
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
      _ <- ProjectFlow.showProjects(context)(telegramApi, tarotApi, sessionService)
    } yield () 

  private val helpText: String =
    """Команды:
      |/start
      |/help
      |""".stripMargin
}
