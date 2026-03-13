package bot.application.handlers.telegram

import bot.application.handlers.telegram.flows.*
import bot.domain.models.session.pending.BotPending
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.SessionRequire
import bot.layers.BotEnv
import zio.ZIO

object TelegramForwardHandler {
  def handle(context: TelegramContext, channelId: Long, channelName: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)

      pending <- SessionRequire.pending(context.chatId)
      _ <- ZIO.logInfo(s"Received forward channel $channelId message from chat ${context.chatId} for pending action $pending")
      
      _ <- pending match {
        case BotPending.ChannelChannelId(channelMode) =>
          ChannelFlow.setChannel(context, channelMode, channelId, channelName)(telegramApi, tarotApi, sessionService)      
        case BotPending.Spread(_) | BotPending.Card(_) | BotPending.CardOfDay(_) =>
          for {
            _ <- ZIO.logError(s"Unknown forward pending action $pending from chat ${context.chatId}")
            _ <- telegramApi.sendText(context.chatId, "Неизвестная команда пересланного сообщения. Введите /help")
          } yield ()
      }
    } yield ()
}
