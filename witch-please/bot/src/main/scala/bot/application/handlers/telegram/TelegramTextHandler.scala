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
        case Some(BotPendingAction.SpreadCardCount(spreadMode, title)) =>
          text.toIntOption match {
            case Some(cardCount) =>
              SpreadFlow.setSpreadCardCount(context, spreadMode, title, cardCount)(telegramApi, tarotApi, sessionService)
            case None =>
              telegramApi.sendText(context.chatId, "Введи число карт числом")
          }
        case Some(BotPendingAction.CardTitle(index)) =>
          CardFlow.setCardTitle(context, index, text)(telegramApi, tarotApi, sessionService)
        case None | Some(BotPendingAction.SpreadPhoto(_,_,_)) | Some(BotPendingAction.CardPhoto(_, _)) =>
          for {
            _ <- ZIO.logInfo(s"Ignored plain text from ${context.chatId}: $text")
            telegramApiService <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
            _ <- telegramApiService.sendText(context.chatId, "Пожалуйста, используйте команды. Введите /help.")
          } yield ()
      }
    } yield ()
}
