package bot.application.handlers.telegram

import bot.application.handlers.telegram.flows.*
import bot.domain.models.session.BotPendingAction
import bot.domain.models.telegram.TelegramContext
import bot.layers.BotEnv
import zio.ZIO

object TelegramForwardHandler {
  def handle(context: TelegramContext, channelId: Long, channelName: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      
      session <- sessionService.get(context.chatId)
      _ <- ZIO.logInfo(s"Received forward channel $channelId message from chat ${context.chatId} for pending action ${session.pending}")
      
      _ <- session.pending match {      
        case Some(BotPendingAction.ChannelChannelId(channelMode)) =>
          ChannelFlow.setChannel(context, channelMode, channelId, channelName)(telegramApi, tarotApi, sessionService)      
        case None             
             | Some(BotPendingAction.SpreadTitle(_)) | Some(BotPendingAction.SpreadCardsCount(_,_)) 
             | Some(BotPendingAction.SpreadDescription(_,_,_)) 
             | Some(BotPendingAction.CardTitle(_)) | Some(BotPendingAction.CardDescription(_,_))
             | Some(BotPendingAction.CardOfDayCardId(_)) | Some(BotPendingAction.CardOfDayTitle(_,_))
             | Some(BotPendingAction.CardOfDayDescription(_,_,_))
             | Some(BotPendingAction.SpreadPhoto(_,_,_,_)) | Some(BotPendingAction.CardPhoto(_,_,_)) 
             | Some(BotPendingAction.CardOfDayPhoto(_,_,_,_))
        =>
          for {
            _ <- ZIO.logError(s"Unknown forward pending action ${session.pending} from chat ${context.chatId}")
            _ <- telegramApi.sendText(context.chatId, "Неизвестная команда пересланного сообщения")
          } yield ()
      }
    } yield ()
}
