package bot.integration.flows

import bot.api.BotApiRoutes
import bot.domain.models.session.BotPendingAction
import bot.layers.BotEnv
import bot.telegram.TestTelegramWebhook
import shared.infrastructure.services.clients.ZIOHttpClient
import zio.http.{Response, Routes}
import zio.{Scope, ZIO}

import java.util.UUID


object ProjectFlow {
  def startProject(app: Routes[BotEnv, Response], chatId: Long): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.createProjectRequest(chatId)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("ProjectName", chatId) { case BotPendingAction.ProjectName => () }
    } yield ()

  def projectName(app: Routes[BotEnv, Response], chatId: Long, name: String): ZIO[Scope & BotEnv, Throwable, Unit] =
    val projectNameRequest = TestTelegramWebhook.textRequest(chatId, name)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), projectNameRequest)
    for {
      _ <- app.runZIO(request)
    } yield ()

  def selectProject(app: Routes[BotEnv, Response], chatId: Long, projectId: UUID): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.selectProjectRequest(chatId, projectId)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      response <- app.runZIO(request)
      _ <- CommonFlow.expectStatusOk(response, "get projects fail")
    } yield ()
}
