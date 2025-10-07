package bot.application.handlers.telegram

import bot.domain.models.session.BotPendingAction
import bot.domain.models.telegram.*
import bot.layers.BotEnv
import zio.*

object TelegramTextHandler {
  def handle(context: TelegramContext, text: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      botSessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)
      telegramApiService <- ZIO.serviceWith[BotEnv](_.botService.telegramChannelService)

      session <- botSessionService.get(context.chatId)
      _ <- ZIO.logInfo(s"Received text from chat ${context.chatId} for pending action ${session.pending}")
      _ <- session.pending match {
        case Some(BotPendingAction.ProjectName) =>
          TelegramPendingHandler.handleProjectName(context, session, text)
        case Some(BotPendingAction.SpreadTitle) =>
          TelegramPendingHandler.handleSpreadTitle(context, session, text)
        case Some(BotPendingAction.SpreadCardCount(title: String)) =>
          TelegramPendingHandler.handleSpreadTitle(context, session, text)
        case None | Some(BotPendingAction.SpreadCover(_, _)) | Some(BotPendingAction.CardCover(_, _)) =>
          for {
            _ <- ZIO.logInfo(s"Ignored plain text from ${context.chatId}: $text")
            telegramApiService <- ZIO.serviceWith[BotEnv](_.botService.telegramChannelService)
            _ <- telegramApiService.sendText(context.chatId, "Пожалуйста, используйте команды. Введите /help.")
          } yield ()
      }
    } yield ()
}
