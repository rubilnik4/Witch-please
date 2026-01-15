package bot.application.handlers.telegram

import bot.application.handlers.telegram.flows.*
import bot.domain.models.session.BotPendingAction
import bot.domain.models.telegram.TelegramContext
import bot.layers.BotEnv
import zio.ZIO

object TelegramPhotoHandler {
  def handle(context: TelegramContext, fileId: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      
      session <- sessionService.get(context.chatId)
      _ <- ZIO.logInfo(s"Received photo from chat ${context.chatId} for pending action ${session.pending}")
      
      _ <- session.pending match {
        case Some(BotPendingAction.SpreadPhoto(spreadMode, title, cardCount, description)) =>
          SpreadFlow.setSpreadPhoto(context, spreadMode, title, cardCount, description, fileId)(telegramApi, tarotApi, sessionService)
        case Some(BotPendingAction.CardPhoto(cardMode, title, description)) =>
          CardFlow.setCardPhoto(context, cardMode,  title, description, fileId)(telegramApi, tarotApi, sessionService)
        case Some(BotPendingAction.CardOfDayPhoto(cardOfDayMode, cardId, description)) =>
          CardOfDayFlow.setCardOfDayPhoto(context, cardOfDayMode, cardId, description, fileId)(telegramApi, tarotApi, sessionService)  
        case None 
             | Some(BotPendingAction.SpreadTitle(_)) | Some(BotPendingAction.SpreadCardsCount(_,_)) 
             | Some(BotPendingAction.SpreadDescription(_,_,_)) 
             | Some(BotPendingAction.CardTitle(_)) | Some(BotPendingAction.CardDescription(_,_))
             | Some(BotPendingAction.CardOfDayCardId(_)) | Some(BotPendingAction.CardOfDayDescription(_,_))
        =>
          for {
            _ <- ZIO.logError(s"Unknown photo pending action ${session.pending} from chat ${context.chatId}")
            _ <- telegramApi.sendText(context.chatId, "Неизвестная команда отправки фото")
          } yield ()
      }
    } yield ()
}
