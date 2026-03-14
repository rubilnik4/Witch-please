package bot.application.handlers.telegram.flows

import bot.domain.models.session.ChannelMode
import bot.domain.models.session.pending.*
import bot.domain.models.telegram.TelegramContext
import bot.layers.BotEnv
import zio.ZIO

object ChannelDraftFlow {
  def setChannelStartDraft(context: TelegramContext, pending: ChannelPending): ZIO[BotEnv, Throwable, Unit] =
    for {
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)

      _ <- pending.draft match {
        case ChannelDraft.Start =>
          val nextDraft = ChannelDraft.AwaitingChannelId
          val nextPending = ChannelPending(pending.mode, nextDraft)
          for {
            _ <- sessionService.setPending(context.chatId, BotPending.Channel(nextPending))
            _ <- sendChannelPendingReply(context, pending)
          } yield ()
        case _ =>
          ZIO.logError(s"Used channel pending $pending instead of start draft chat=${context.chatId}") *>
            ZIO.fail(new IllegalStateException(s"Used channel pending $pending instead of start draft"))
      }
    } yield ()

  def setChannelForwardDraft(context: TelegramContext, channelId: Long, name: String, pending: ChannelPending): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)

      _ <- pending.draft match {
        case ChannelDraft.AwaitingChannelId =>
          val nextDraft = ChannelDraft.Complete(channelId, name)
          val nextPending = ChannelPending(pending.mode, nextDraft)
          setChannelCompleteDraft(context, nextPending)
        case _ =>
          ZIO.logError(s"Used forward instead of channel awaiting id in chat ${context.chatId}") *>
            telegramApi.sendText(context.chatId, "Принимаю только пересланный пост из канала").unit
      }
    } yield ()

  private def setChannelCompleteDraft(context: TelegramContext, pending: ChannelPending): ZIO[BotEnv, Throwable, Unit] =
    pending.draft match {
      case ChannelDraft.Complete(channelId, name) =>
        ChannelFlow.submitChannel(context, pending.mode, channelId, name)
      case _ =>
        ZIO.logError(s"Used channel pending $pending instead of complete draft in chat ${context.chatId}") *>
          ZIO.fail(new IllegalStateException(s"Used channel pending $pending instead of complete draft"))
    }

  private def sendChannelPendingReply(context: TelegramContext, pending: ChannelPending): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)

      _ <- pending.mode match {
        case ChannelMode.Create =>
          telegramApi.sendText(context.chatId, "Добавь бот в свой канал и перешли ему любой пост из этого канала")
        case ChannelMode.Edit(_) =>
          telegramApi.sendText(context.chatId, "Перешли новый пост из канала, чтобы обновить привязку")
      }
    } yield ()
}
