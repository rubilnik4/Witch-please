package bot.integration.flows

import bot.api.BotApiRoutes
import bot.domain.models.session.BotPendingAction
import bot.layers.BotEnv
import bot.telegram.TestTelegramWebhook
import shared.infrastructure.services.clients.ZIOHttpClient
import zio.http.{Response, Routes}
import zio.{Scope, ZIO}

import java.util.UUID


object SpreadFlow {
  def startSpread(app: Routes[BotEnv, Response], chatId: Long): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.createSpreadRequest(chatId)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("SpreadTitle", chatId) { case BotPendingAction.SpreadTitle => () }
    } yield ()

  def spreadTitle(app: Routes[BotEnv, Response], chatId: Long, title: String): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, title)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("SpreadCardCount", chatId) { case BotPendingAction.SpreadCardCount(title) => title }
    } yield ()

  def spreadCardCount(app: Routes[BotEnv, Response], chatId: Long, cardCount: Int): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, cardCount.toString)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("SpreadPhoto", chatId) { case BotPendingAction.SpreadPhoto(cardCount,_) => cardCount }
    } yield ()

  def selectSpread(app: Routes[BotEnv, Response], chatId: Long, spreadId: UUID, cardCount: Int): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.selectSpreadsRequest(chatId, spreadId, cardCount)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      response <- app.runZIO(request)
      _ <- CommonFlow.expectStatusOk(response, "get spreads fail")
    } yield ()
}
