package bot.application.handlers.telegram

import bot.application.handlers.telegram.flows.*
import bot.domain.models.session.pending.BotPending
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
        case Some(BotPending.ChannelChannelId(channelMode)) =>
          ChannelFlow.setChannel(context, channelMode, channelId, channelName)(telegramApi, tarotApi, sessionService)      
        case None
             | Some(BotPending.Spread(_))
             | Some(BotPending.CardTitle(_)) | Some(BotPending.CardDescription(_,_))
             | Some(BotPending.CardOfDayCardId(_)) | Some(BotPending.CardOfDayTitle(_,_))
             | Some(BotPending.CardOfDayDescription(_,_,_))
             | Some(BotPending.CardPhoto(_,_,_))
             | Some(BotPending.CardOfDayPhoto(_,_,_,_))
        =>
          for {
            _ <- ZIO.logError(s"Unknown forward pending action ${session.pending} from chat ${context.chatId}")
            _ <- telegramApi.sendText(context.chatId, "Неизвестная команда пересланного сообщения")
          } yield ()
      }
    } yield ()
}
