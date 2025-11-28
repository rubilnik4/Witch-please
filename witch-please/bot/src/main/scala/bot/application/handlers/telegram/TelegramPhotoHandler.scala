package bot.application.handlers.telegram

import bot.application.handlers.telegram.flows.*
import bot.domain.models.session.BotPendingAction
import bot.domain.models.telegram.TelegramContext
import bot.layers.BotEnv
import zio.ZIO

object TelegramPhotoHandler {
  def handle(context: TelegramContext, fileId: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.botService.telegramApiService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.botService.tarotApiService)
      sessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)
      
      session <- sessionService.get(context.chatId)
      _ <- ZIO.logInfo(s"Received photo from chat ${context.chatId} for pending action ${session.pending}")
      
      _ <- session.pending match {
        case Some(BotPendingAction.SpreadPhoto(spreadMode, title, cardCount)) =>
          SpreadFlow.setSpreadPhoto(context, spreadMode, title, cardCount, fileId)(telegramApi, tarotApi, sessionService)
        case Some(BotPendingAction.CardPhoto(index, description)) =>
          CardFlow.setCardPhoto(context, index, description, fileId)(telegramApi, tarotApi, sessionService)
        case None | Some(BotPendingAction.SpreadTitle(_)) 
             | Some(BotPendingAction.SpreadCardCount(_,_)) | Some(BotPendingAction.CardTitle(_)) =>
          for {
            _ <- ZIO.logError(s"Unknown photo pending action ${session.pending} from chat ${context.chatId}")
            _ <- telegramApi.sendText(context.chatId, "Неизвестная команда отправки фото")
          } yield ()
      }
    } yield ()
}
