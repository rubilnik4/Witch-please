package bot.application.handlers.telegram

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
          TelegramPendingHandler.handleProjectName(context, text)(telegramApi, tarotApi, sessionService)
        case Some(BotPendingAction.SpreadTitle) =>
          TelegramPendingHandler.handleSpreadTitle(context, text)(telegramApi, tarotApi, sessionService)
        case Some(BotPendingAction.SpreadCardCount(title: String)) =>
          text.toIntOption match {
            case Some(cardCount) =>
              TelegramPendingHandler.handleSpreadCardCount(context, title, cardCount)(
                telegramApi, tarotApi, sessionService)
            case None =>
              telegramApi.sendText(context.chatId, "Введи число карт числом")
          }
        case Some(BotPendingAction.CardIndex) =>
          text.toIntOption match {
            case Some(userCardIndex) =>
              val cardIndex = userCardIndex - 1
              TelegramPendingHandler.handleCardIndex(context, cardIndex)(telegramApi, tarotApi, sessionService)
            case None =>
              telegramApi.sendText(context.chatId, "Введи номер карты числом")
          }
        case Some(BotPendingAction.CardDescription(index: Int)) =>
          TelegramPendingHandler.handleCardDescription(context, index, text)(telegramApi, tarotApi, sessionService)
        case None | Some(BotPendingAction.SpreadPhoto(_, _)) | Some(BotPendingAction.CardPhoto(_, _)) =>
          for {
            _ <- ZIO.logInfo(s"Ignored plain text from ${context.chatId}: $text")
            telegramApiService <- ZIO.serviceWith[BotEnv](_.botService.telegramApiService)
            _ <- telegramApiService.sendText(context.chatId, "Пожалуйста, используйте команды. Введите /help.")
          } yield ()
      }
    } yield ()
}
