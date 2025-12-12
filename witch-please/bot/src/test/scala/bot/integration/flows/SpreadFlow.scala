package bot.integration.flows

import bot.api.BotApiRoutes
import bot.domain.models.session.{BotPendingAction, SpreadMode}
import bot.layers.BotEnv
import bot.telegram.TestTelegramWebhook
import shared.infrastructure.services.clients.ZIOHttpClient
import zio.http.{Response, Routes}
import zio.{Scope, ZIO}

import java.util.UUID


object SpreadFlow {
  def startSpread(app: Routes[BotEnv, Response], chatId: Long, spreadMode: SpreadMode): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.createSpreadRequest(chatId)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("SpreadTitle", chatId) {
        case BotPendingAction.SpreadTitle(mode) if mode == spreadMode => () }
    } yield ()

  def spreadTitle(app: Routes[BotEnv, Response], chatId: Long, spreadMode: SpreadMode, title: String): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, title)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("SpreadCardCount", chatId) {
        case BotPendingAction.SpreadCardsCount(mode, t) if mode == spreadMode && t == title => t }
    } yield ()

  def spreadCardCount(app: Routes[BotEnv, Response], chatId: Long, spreadMode: SpreadMode, cardCount: Int): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, cardCount.toString)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("SpreadDescription", chatId) {
        case BotPendingAction.SpreadDescription(mode, _, c) if mode == spreadMode && c == cardCount => c }
    } yield ()

  def spreadDescription(app: Routes[BotEnv, Response], chatId: Long, spreadMode: SpreadMode, description: String): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, description.toString)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("SpreadPhoto", chatId) {
        case BotPendingAction.SpreadPhoto(mode, _, _, d) if mode == spreadMode && d == description => d
      }
    } yield ()
    
  def selectSpread(app: Routes[BotEnv, Response], chatId: Long, spreadId: UUID, cardCount: Int): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.selectSpreadsRequest(chatId, spreadId, cardCount)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      response <- app.runZIO(request)
      _ <- CommonFlow.expectStatusOk(response, "get spreads fail")
    } yield ()
}
