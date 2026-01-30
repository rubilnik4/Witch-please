package bot.integration.flows

import bot.api.BotApiRoutes
import bot.domain.models.session.{BotPendingAction, ChannelMode}
import bot.layers.BotEnv
import bot.telegram.TestTelegramWebhook
import shared.infrastructure.services.clients.ZIOHttpClient
import zio.http.{Response, Routes}
import zio.{Scope, ZIO}

import java.util.UUID

object ChannelFlow {
  def startChannel(app: Routes[BotEnv, Response], chatId: Long, channelMode: ChannelMode): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = channelMode match {
      case ChannelMode.Create =>
        TestTelegramWebhook.createChannelRequest(chatId)
      case ChannelMode.Edit(userChannelId) =>
        TestTelegramWebhook.updateChannelRequest(chatId, userChannelId)
    }
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("ChannelChannelId", chatId) {
        case BotPendingAction.ChannelChannelId(mode) if mode == channelMode => () }
    } yield ()

  def channelChannelId(app: Routes[BotEnv, Response], chatId: Long, channelId: Long): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.forwardRequest(chatId, channelId)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectNoPending(chatId) 
    } yield ()
}
