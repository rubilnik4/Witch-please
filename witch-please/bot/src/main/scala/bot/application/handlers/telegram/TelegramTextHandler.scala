package bot.application.handlers.telegram

import bot.application.handlers.telegram.flows.*
import bot.domain.models.session.BotPendingAction
import bot.domain.models.telegram.*
import bot.layers.BotEnv
import zio.*

object TelegramTextHandler {
  def handle(context: TelegramContext, text: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)

      session <- sessionService.get(context.chatId)
      _ <- ZIO.logInfo(s"Received text from chat ${context.chatId} for pending action ${session.pending}")

      _ <- session.pending match {
        case Some(BotPendingAction.SpreadTitle(spreadMode)) =>
          SpreadFlow.setSpreadTitle(context, spreadMode, text)(telegramApi, tarotApi, sessionService)
        case Some(BotPendingAction.SpreadCardsCount(spreadMode, title)) =>
          text.toIntOption match {
            case Some(cardCount) if cardCount > 0 =>
              SpreadFlow.setSpreadCardsCount(context, spreadMode, title, cardCount)(telegramApi, tarotApi, sessionService)
            case _ =>
              telegramApi.sendText(context.chatId, "Число карт должно быть больше 0")
          }
        case Some(BotPendingAction.SpreadDescription(spreadMode, title, cardsCount)) =>
          SpreadFlow.setSpreadDescription(context, spreadMode, title, cardsCount, text)(telegramApi, tarotApi, sessionService)  
        case Some(BotPendingAction.CardTitle(cardMode)) =>
          CardFlow.setCardTitle(context, cardMode, text)(telegramApi, tarotApi, sessionService)
        case Some(BotPendingAction.CardDescription(cardMode, title)) =>
          CardFlow.setCardDescription(context, cardMode, title, text)(telegramApi, tarotApi, sessionService)
        case Some(BotPendingAction.CardOfDayCardId(cardOfDayMode)) =>
          text.toIntOption match {
            case Some(position) if position > 0 =>
              CardOfDayFlow.setCardOfDayCardId(context, cardOfDayMode, position - 1)(telegramApi, tarotApi, sessionService)
            case _ =>
              telegramApi.sendText(context.chatId, "Введи номер карты числом и больше 0")
          }
        case Some(BotPendingAction.CardOfDayDescription(cardMode, cardId)) =>
          CardOfDayFlow.setCardOfDayDescription(context, cardMode, cardId, text)(telegramApi, tarotApi, sessionService)  
        case None
             | Some(BotPendingAction.SpreadPhoto(_,_,_,_)) | Some(BotPendingAction.CardPhoto(_,_,_))
             | Some(BotPendingAction.CardOfDayPhoto(_,_,_))=>
          for {
            _ <- ZIO.logInfo(s"Ignored plain text from ${context.chatId}: $text")
            telegramApiService <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
            _ <- telegramApiService.sendText(context.chatId, "Пожалуйста, используйте команды. Введите /help.")
          } yield ()
      }
    } yield ()
}
