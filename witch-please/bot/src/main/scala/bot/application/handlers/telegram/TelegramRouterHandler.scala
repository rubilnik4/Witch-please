package bot.application.handlers.telegram

import bot.application.commands.*
import bot.application.handlers.telegram.flows.*
import bot.application.handlers.telegram.parsers.TelegramCommandParser
import bot.domain.models.telegram.*
import bot.layers.BotEnv
import zio.ZIO

object TelegramRouterHandler {
  def handle(context: TelegramContext, command: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Received command from chat ${context.chatId}: $command")
      
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      
      _ <- TelegramCommandParser.handle(command) match {
        case BotCommand.Start =>
          StartFlow.handleStart(context)
        case authorCommand: AuthorCommand =>
          handleAuthorCommand(context, authorCommand)
        case clientCommand: ClientCommand =>
          handleClientCommand(context, clientCommand)
        case scheduleCommand: ScheduleCommand =>
          SchedulerFlow.handle(context, scheduleCommand)
        case BotCommand.ShowPhoto(photoId) =>
          PhotoFlow.showPhoto(context, photoId)
        case BotCommand.Noop =>
          ZIO.logInfo(s"Empty noop command for chat ${context.chatId}")
        case BotCommand.Help =>
          telegramApi.sendText(context.chatId, helpText)
        case BotCommand.Unknown =>
          telegramApi.sendText(context.chatId, "Неизвестная команда. Введите /help.")  
      }
    } yield ()

  private def handleAuthorCommand(context: TelegramContext, command: AuthorCommand): ZIO[BotEnv, Throwable, Unit] =
      for {
        _ <- command match {
          case AuthorCommand.Start =>
            StartFlow.handleAuthorStart(context)
          case AuthorCommand.CreateChannel =>
            ChannelFlow.createChannel(context)
          case AuthorCommand.EditChannel(userChannelId) =>
            ChannelFlow.editChannel(context, userChannelId)
          case AuthorCommand.CreateSpread =>
            SpreadFlow.createSpread(context)
          case AuthorCommand.EditSpread(spreadId) =>
            SpreadFlow.editSpread(context, spreadId)
          case AuthorCommand.CloneSpread(spreadId) =>
            SpreadFlow.cloneSpread(context, spreadId)
          case AuthorCommand.SelectSpreads =>
            SpreadFlow.selectSpreads(context)
          case AuthorCommand.SelectSpread(spreadId) =>
            SpreadFlow.selectSpread(context, spreadId)
          case AuthorCommand.DeleteSpread(spreadId) =>
            SpreadFlow.deleteSpread(context)
          case AuthorCommand.PublishSpread(spreadId) =>
            PublishFlow.publishSpread(context)
          case AuthorCommand.CreateCard(position) =>
            CardFlow.createCard(context, position)
          case AuthorCommand.EditCard(cardId) =>
            CardFlow.editCard(context, cardId)
          case AuthorCommand.DeleteCard(cardId) =>
            CardFlow.deleteCard(context, cardId)
          case AuthorCommand.SelectCards(spreadId) =>
            CardFlow.selectCards(context, spreadId)
          case AuthorCommand.SelectCard(cardId) =>
            CardFlow.selectCard(context, cardId)
          case AuthorCommand.CreateCardOfDay =>
            CardOfDayFlow.createCardOfDay(context)
          case AuthorCommand.EditCardOfDay(cardOfDayId) =>
            CardOfDayFlow.editCardOfDay(context, cardOfDayId)
          case AuthorCommand.DeleteCardOfDay(cardOfDayId) =>
            CardOfDayFlow.deleteCardOfDay(context, cardOfDayId)
          case AuthorCommand.SelectCardOfDay(spreadId) =>
            CardOfDayFlow.selectCardOfDay(context, spreadId)
          case AuthorCommand.KeepCurrent =>
            TelegramKeepCurrentHandler.handleKeepCurrent(context)
        }
      } yield ()

  private def handleClientCommand(context: TelegramContext, command: ClientCommand): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- command match {
        case ClientCommand.Start =>
          StartFlow.handleClientStart(context)
        case ClientCommand.SelectAuthor(authorId) =>
          StartFlow.handleClientStart(context)
      }
    } yield ()

  private val helpText: String =
    """Команды:
      |/start
      |/help
      |""".stripMargin
}
