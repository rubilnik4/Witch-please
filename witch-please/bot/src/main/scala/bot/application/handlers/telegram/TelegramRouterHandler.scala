package bot.application.handlers.telegram

import bot.application.commands.*
import bot.application.handlers.telegram.flows.*
import bot.domain.models.telegram.*
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.infrastructure.services.telegram.TelegramApiService
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
            StartFlow.handleStart(context)(telegramApi, tarotApi, sessionService)
          case TarotCommand.AdminStart =>
            StartFlow.handleAuthorStart(context)(telegramApi, tarotApi, sessionService)
          case TarotCommand.UserStart =>
            StartFlow.handleClientStart(context)(telegramApi, tarotApi, sessionService)            
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

  private val helpText: String =
    """Команды:
      |/start
      |/help
      |""".stripMargin
}
