package bot.integration.flows

import bot.api.BotApiRoutes
import bot.domain.models.session.SpreadMode
import bot.domain.models.session.pending.*
import bot.layers.BotEnv
import bot.telegram.TestTelegramWebhook
import shared.infrastructure.services.clients.ZIOHttpClient
import zio.http.{Response, Routes}
import zio.{Scope, ZIO}

import java.util.UUID

object SpreadFlow {
  def startSpread(app: Routes[BotEnv, Response], chatId: Long, spreadMode: SpreadMode): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = spreadMode match {
      case SpreadMode.Create =>
        TestTelegramWebhook.createSpreadRequest(chatId)
      case SpreadMode.Edit(spreadId) =>
        TestTelegramWebhook.updateSpreadRequest(chatId, spreadId)
    }
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("SpreadTitle", chatId) {
        case BotPending.Spread(SpreadPending(mode, SpreadDraft.AwaitingTitle)) if mode == spreadMode => () }
    } yield ()

  def spreadTitle(app: Routes[BotEnv, Response], chatId: Long, title: String): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, title)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("SpreadCardCount", chatId) {
        case BotPending.Spread(SpreadPending(mode, SpreadDraft.AwaitingCardsCount(t))) if t == title => t }
    } yield ()

  def spreadCardCount(app: Routes[BotEnv, Response], chatId: Long, cardCount: Int): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, cardCount.toString)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("SpreadDescription", chatId) {
        case BotPending.Spread(SpreadPending(mode, SpreadDraft.AwaitingDescription(_,c))) if c == cardCount => c }
    } yield ()

  def spreadDescription(app: Routes[BotEnv, Response], chatId: Long, description: String): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, description.toString)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("SpreadPhoto", chatId) {
        case BotPending.Spread(SpreadPending(mode, SpreadDraft.AwaitingPhoto(_,_,d))) if d == description => d
      }
    } yield ()
    
  def selectSpread(app: Routes[BotEnv, Response], chatId: Long, spreadId: UUID): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.selectSpreadRequest(chatId, spreadId)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      response <- app.runZIO(request)
      _ <- CommonFlow.expectStatusOk(response, "select spread fail")
    } yield ()
}
