package bot.integration.flows

import bot.api.BotApiRoutes
import bot.domain.models.session.BotPendingAction
import bot.layers.BotEnv
import bot.telegram.TestTelegramWebhook
import shared.infrastructure.services.clients.ZIOHttpClient
import zio.{IO, Scope, ZIO}
import zio.http.{Response, Routes, Status}

object CommonFlow {
  def sendPhoto(app: Routes[BotEnv, Response], chatId: Long, fileId: String): ZIO[Scope & BotEnv, Nothing, Response] =
    val postRequest = TestTelegramWebhook.photoRequest(chatId, fileId)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      response <- app.runZIO(request)
    } yield response

  def expectPending[A](name: String, chatId: Long)(
    pending: PartialFunction[BotPendingAction, A]): ZIO[BotEnv, Throwable, A] =
    for {
      botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      session <- botSessionService.get(chatId)
      got <- ZIO.fromOption(session.pending).orElseFail(new RuntimeException(s"pending is empty, expected $name"))
      result <- if (pending.isDefinedAt(got)) ZIO.succeed(pending(got))
        else ZIO.fail(new RuntimeException(s"pending mismatch: expected $name, got $got"))
    } yield result

  def expectNoPending(chatId: Long): ZIO[BotEnv, Throwable, Unit] =
    for {
      botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      session <- botSessionService.get(chatId)
      _ <- ZIO.fail(new RuntimeException(s"pending must be empty, got ${session.pending}"))
        .unless(session.pending.isEmpty)
    } yield ()

  def expectStatusOk(response: Response, clue: String): IO[Throwable, Unit] =
    for {
      _ <- ZIO.fail(new RuntimeException(s"$clue: status=${response.status}"))
        .unless(response.status == Status.Ok)
    } yield ()
}
