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
    val postRequest = TestTelegramWebhook.textRequest(chatId, "Test spread")
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("CardIndex", chatId) { case BotPendingAction.SpreadTitle => () }
    } yield ()

  def spreadTitle(app: Routes[BotEnv, Response], chatId: Long, title: String): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, title)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("CardDescription", chatId) { case BotPendingAction.SpreadCardCount(title) => title }
    } yield ()

  def spreadCardCount(app: Routes[BotEnv, Response], chatId: Long, cardCount: Int): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, cardCount.toString)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("CardDescription", chatId) { case BotPendingAction.SpreadPhoto(cardCount,_) => cardCount }
    } yield ()

  def getSpreads(app: Routes[BotEnv, Response], chatId: Long, projectId: UUID): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.getSpreadsRequest(chatId, projectId)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      response <- app.runZIO(request)
      _ <- CommonFlow.expectStatusOk(response, "get spreads fail")
    } yield ()  
}
