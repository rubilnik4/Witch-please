package bot.integration.flows

import bot.api.BotApiRoutes
import bot.domain.models.session.{BotPendingAction, CardOfDayMode, CardPosition}
import bot.layers.BotEnv
import bot.telegram.TestTelegramWebhook
import shared.infrastructure.services.clients.ZIOHttpClient
import zio.http.{Response, Routes}
import zio.{Scope, ZIO}

import java.util.UUID

object CardOfDayFlow {
  def startCardOfDay(app: Routes[BotEnv, Response], chatId: Long, cardOfDayMode: CardOfDayMode): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = cardOfDayMode match {
      case CardOfDayMode.Create =>
        TestTelegramWebhook.createCardOfDayRequest(chatId)
      case CardOfDayMode.Edit(cardOfDayId) =>
        TestTelegramWebhook.updateCardOfDayRequest(chatId, cardOfDayId)
    }
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("CardOfDayCardId", chatId) {
        case BotPendingAction.CardOfDayCardId(mode) if mode == cardOfDayMode => () }
    } yield ()

  def cardOfDayCardId(app: Routes[BotEnv, Response], chatId: Long, cardOfDayMode: CardOfDayMode, cardPosition: CardPosition): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, (cardPosition.position + 1).toString)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("CardOfDayDescription", chatId) {
        case BotPendingAction.CardOfDayDescription(mode, cardId) if mode == cardOfDayMode && cardId == cardPosition.cardId => cardId }
    } yield ()

  def cardOfDayDescription(app: Routes[BotEnv, Response], chatId: Long, cardOfDayMode: CardOfDayMode, description: String): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, description)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("CardOfDayPhoto", chatId) {
        case BotPendingAction.CardOfDayPhoto(mode, _, d) if mode == cardOfDayMode && d == description => d }
    } yield ()

  def selectCardOfDay(app: Routes[BotEnv, Response], chatId: Long, spreadId: UUID): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.selectCardOfDayRequest(chatId, spreadId)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      response <- app.runZIO(request)
      _ <- CommonFlow.expectStatusOk(response, "select card of day fail")
    } yield ()  
}
