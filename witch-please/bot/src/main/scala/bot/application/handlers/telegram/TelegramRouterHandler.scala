package bot.application.handlers.telegram

import bot.application.commands.*
import bot.application.handlers.telegram.flows.*
import bot.application.handlers.telegram.parsers.TelegramCommandParser
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
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)

      _ <- ZIO.logInfo(s"Received command from chat ${context.chatId}: $command")
      _ <- TelegramCommandParser.handle(command) match {
        case BotCommand.Start =>
          StartFlow.handleStart(context)(telegramApi, tarotApi, sessionService)
        case authorCommand: AuthorCommand =>
          handleAuthorCommand(context, authorCommand)(telegramApi, tarotApi, sessionService)
        case clientCommand: ClientCommand =>
          handleClientCommand(context, clientCommand)(telegramApi, tarotApi, sessionService)
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

  private def handleAuthorCommand(context: TelegramContext, command: AuthorCommand)
    (telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
      for {
        _ <- command match {
          case AuthorCommand.Start =>
            StartFlow.handleAuthorStart(context)(telegramApi, tarotApi, sessionService)
          case AuthorCommand.CreateChannel =>
            ChannelFlow.createChannel(context)(telegramApi, tarotApi, sessionService)
          case AuthorCommand.EditChannel(userChannelId) =>
            ChannelFlow.editChannel(context, userChannelId)(telegramApi, sessionService)
          case AuthorCommand.CreateSpread =>
            SpreadFlow.createSpread(context)(telegramApi, sessionService)
          case AuthorCommand.EditSpread(spreadId) =>
            SpreadFlow.editSpread(context, spreadId)(telegramApi, sessionService)  
          case AuthorCommand.SelectSpread(spreadId) =>
            SpreadFlow.selectSpread(context, spreadId)(telegramApi, tarotApi, sessionService)
          case AuthorCommand.SelectSpreadCards(spreadId) =>
            CardFlow.selectSpreadCards(context, spreadId)(telegramApi, tarotApi, sessionService)
          case AuthorCommand.SelectSpreadCardOfDay(spreadId) =>
            CardOfDayFlow.selectSpreadCardOfDay(context, spreadId)(telegramApi, tarotApi, sessionService)
          case AuthorCommand.DeleteSpread(spreadId) =>
            SpreadFlow.deleteSpread(context)(telegramApi, tarotApi, sessionService)
          case AuthorCommand.PublishSpread(spreadId) =>
            PublishFlow.publishSpread(context)(telegramApi, tarotApi, sessionService)
          case AuthorCommand.CreateCard(position) =>
            CardFlow.createCard(context, position)(telegramApi, sessionService)
          case AuthorCommand.EditCard(cardId) =>
            CardFlow.editCard(context, cardId)(telegramApi, sessionService)
          case AuthorCommand.DeleteCard(cardId) =>
            CardFlow.deleteCard(context, cardId)(telegramApi, tarotApi, sessionService)
          case AuthorCommand.SelectCard(cardId) =>
            CardFlow.selectCard(context, cardId)(telegramApi, tarotApi, sessionService)
          case AuthorCommand.CreateCardOfDay =>
            CardOfDayFlow.createCardOfDay(context)(telegramApi, sessionService)
          case AuthorCommand.EditCardOfDay(cardOfDayId) =>
            CardOfDayFlow.editCardOfDay(context, cardOfDayId)(telegramApi, sessionService)
          case AuthorCommand.DeleteCardOfDay(cardOfDayId) =>
            CardOfDayFlow.deleteCardOfDay(context, cardOfDayId)(telegramApi, tarotApi, sessionService)  
        }
      } yield ()

  private def handleClientCommand(context: TelegramContext, command: ClientCommand)
      (telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- command match {
        case ClientCommand.Start =>
          StartFlow.handleClientStart(context)(telegramApi, tarotApi, sessionService)
        case ClientCommand.SelectAuthor(authorId) =>
          StartFlow.handleClientStart(context)(telegramApi, tarotApi, sessionService)
      }
    } yield ()

  private val helpText: String =
    """Команды:
      |/start
      |/help
      |""".stripMargin
}
