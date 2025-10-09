package bot.integration.flows

import bot.api.BotApiRoutes
import bot.domain.models.session.BotPendingAction
import bot.layers.BotEnv
import bot.telegram.TestTelegramWebhook
import shared.infrastructure.services.clients.ZIOHttpClient
import zio.{Scope, ZIO}
import zio.http.{Response, Routes}


object CardFlow {
  def startCard(app: Routes[BotEnv, Response], chatId: Long): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.createCardRequest(chatId)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("CardIndex", chatId) { case BotPendingAction.CardIndex => () }
    } yield ()

  def cardIndex(app: Routes[BotEnv, Response], chatId: Long, index: Int): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, index.toString)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("CardDescription", chatId) { case BotPendingAction.CardDescription(index) => index }
    } yield ()

  def cardDescription(app: Routes[BotEnv, Response], chatId: Long, desc: String): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, desc)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("CardPhoto", chatId) { case BotPendingAction.CardPhoto(index, description) => index }
    } yield ()

  def getSpreads(app: Routes[BotEnv, Response], chatId: Long): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.getProjectsRequest(chatId)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      response <- app.runZIO(request)
      _ <- CommonFlow.expectStatusOk(response, "get projects fail")
    } yield ()  
}
