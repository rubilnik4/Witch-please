package bot.application.handlers.telegram

import bot.domain.models.session.BotPendingAction
import bot.domain.models.telegram.TelegramContext
import bot.layers.BotEnv
import zio.ZIO

object TelegramPhotoHandler {
  def handle(context: TelegramContext, fileId: String): ZIO[BotEnv, Throwable, Unit] =
    for {     
      botSessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)
      telegramApiService <- ZIO.serviceWith[BotEnv](_.botService.telegramChannelService)
      
      session <- botSessionService.get(context.chatId)
      _ <- ZIO.logInfo(s"Received photo (fileId: $fileId) from chat ${context.chatId} for pending action ${session.pending}")
      _ <- session.pending match {
        case Some(BotPendingAction.SpreadCover(title, cardCount)) =>
          TelegramPendingHandler.handleSpreadCover(context, session, title, cardCount, fileId)
        case Some(BotPendingAction.CardCover(description, index)) =>
          TelegramPendingHandler.handleCardCover(context, session, description, index, fileId)
        case None | Some(BotPendingAction.ProjectName) | Some(BotPendingAction.SpreadTitle) |
             Some(BotPendingAction.SpreadCardCount(_)) =>
          for {
            _ <- ZIO.logError(s"Unknown photo pending action ${session.pending} from chat ${context.chatId}")
            _ <- telegramApiService.sendText(context.chatId, "Неизвестная команда отправки фото")
          } yield ()
      }
    } yield ()
}
