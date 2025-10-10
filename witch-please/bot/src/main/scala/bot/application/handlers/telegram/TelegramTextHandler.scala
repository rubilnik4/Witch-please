package bot.application.handlers.telegram

import bot.application.handlers.telegram.flows.*
import bot.domain.models.session.BotPendingAction
import bot.domain.models.telegram.*
import bot.layers.BotEnv
import zio.*

object TelegramTextHandler {
  def handle(context: TelegramContext, text: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.botService.telegramApiService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.botService.tarotApiService)
      sessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)

      session <- sessionService.get(context.chatId)
      _ <- ZIO.logInfo(s"Received text from chat ${context.chatId} for pending action ${session.pending}")

      _ <- session.pending match {
        case Some(BotPendingAction.ProjectName) =>
          ProjectFlow.setProjectName(context, text)(telegramApi, tarotApi, sessionService)
        case Some(BotPendingAction.SpreadTitle) =>
          SpreadFlow.setSpreadTitle(context, text)(telegramApi, tarotApi, sessionService)
        case Some(BotPendingAction.SpreadCardCount(title: String)) =>
          text.toIntOption match {
            case Some(cardCount) =>
              SpreadFlow.setSpreadCardCount(context, title, cardCount)(telegramApi, tarotApi, sessionService)
            case None =>
              telegramApi.sendText(context.chatId, "Введи число карт числом")
          }
        case Some(BotPendingAction.CardIndex) =>
          text.toIntOption match {
            case Some(userCardIndex) =>
              val cardIndex = userCardIndex - 1
              CardFlow.setCardIndex(context, cardIndex)(telegramApi, tarotApi, sessionService)
            case None =>
              telegramApi.sendText(context.chatId, "Введи номер карты числом")
          }
        case Some(BotPendingAction.CardDescription(index: Int)) =>
          CardFlow.setCardDescription(context, index, text)(telegramApi, tarotApi, sessionService)
        case None | Some(BotPendingAction.SpreadPhoto(_, _)) | Some(BotPendingAction.CardPhoto(_, _)) =>
          for {
            _ <- ZIO.logInfo(s"Ignored plain text from ${context.chatId}: $text")
            telegramApiService <- ZIO.serviceWith[BotEnv](_.botService.telegramApiService)
            _ <- telegramApiService.sendText(context.chatId, "Пожалуйста, используйте команды. Введите /help.")
          } yield ()
      }
    } yield ()
}
