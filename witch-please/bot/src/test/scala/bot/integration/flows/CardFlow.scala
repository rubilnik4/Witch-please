package bot.integration.flows

import bot.api.BotApiRoutes
import bot.domain.models.session.{BotPendingAction, CardMode}
import bot.layers.BotEnv
import bot.telegram.TestTelegramWebhook
import shared.infrastructure.services.clients.ZIOHttpClient
import zio.{Scope, ZIO}
import zio.http.{Response, Routes}

import java.util.UUID


object CardFlow {
  def startCard(app: Routes[BotEnv, Response], chatId: Long, cardMode: CardMode, position: Int): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.createCardRequest(chatId, position)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("CardTitle", chatId) {
        case BotPendingAction.CardTitle(mode) if mode == cardMode => () }
    } yield ()

  def cardTitle(app: Routes[BotEnv, Response], chatId: Long, cardMode: CardMode, title: String): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, title)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("CardDescription", chatId) {
        case BotPendingAction.CardDescription(mode, t) if mode == cardMode && t == title => t }
    } yield ()

  def cardDescription(app: Routes[BotEnv, Response], chatId: Long, cardMode: CardMode, description: String): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, description)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("CardPhoto", chatId) {
        case BotPendingAction.CardPhoto(mode, _, d) if mode == cardMode && d == description => d }
    } yield ()
}
