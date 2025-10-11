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
  def startCard(app: Routes[BotEnv, Response], chatId: Long, index: Int): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.createCardRequest(chatId, index)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("CardIndex", chatId) { case BotPendingAction.CardDescription(i) => i }
    } yield ()

  def cardDescription(app: Routes[BotEnv, Response], chatId: Long, desc: String): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, desc)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("CardPhoto", chatId) { case BotPendingAction.CardPhoto(index, description) => index }
    } yield ()
}
