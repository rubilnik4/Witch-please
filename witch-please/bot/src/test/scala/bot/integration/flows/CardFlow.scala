package bot.integration.flows

import bot.api.BotApiRoutes
import bot.domain.models.session.BotPendingAction
import bot.layers.BotEnv
import bot.telegram.TestTelegramWebhook
import shared.infrastructure.services.clients.ZIOHttpClient
import zio.{Scope, ZIO}
import zio.http.{Response, Routes}

import java.util.UUID


object CardFlow {
  def startCard(app: Routes[BotEnv, Response], chatId: Long, position: Int): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.createCardRequest(chatId, position)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("CardTitle", chatId) { case BotPendingAction.CardTitle(p) => p }
    } yield ()

  def cardDescription(app: Routes[BotEnv, Response], chatId: Long, desc: String): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, desc)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("CardPhoto", chatId) { case BotPendingAction.CardPhoto(position, description) => position }
    } yield ()
}
