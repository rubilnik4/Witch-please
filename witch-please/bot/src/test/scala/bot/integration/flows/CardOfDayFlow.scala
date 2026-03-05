package bot.integration.flows

import bot.api.BotApiRoutes
import bot.domain.models.session.CardOfDayMode
import bot.domain.models.session.pending.BotPending
import bot.layers.BotEnv
import bot.telegram.TestTelegramWebhook
import shared.infrastructure.services.clients.ZIOHttpClient
import shared.models.tarot.cards.CardPosition
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
        case BotPending.CardOfDayCardId(mode) if mode == cardOfDayMode => () }
    } yield ()

  def cardOfDayCardId(app: Routes[BotEnv, Response], chatId: Long, cardPosition: CardPosition): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, (cardPosition.position + 1).toString)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("CardOfDayTitle", chatId) {
        case BotPending.CardOfDayTitle(mode, cardId) if cardId == cardPosition.cardId => cardId }
    } yield ()

  def cardOfDayTitle(app: Routes[BotEnv, Response], chatId: Long, title: String): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, title)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("CardOfDayDescription", chatId) {
        case BotPending.CardOfDayDescription(mode, _, t) if t == title => title
      }
    } yield ()

  def cardOfDayDescription(app: Routes[BotEnv, Response], chatId: Long, description: String): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.textRequest(chatId, description)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
      _ <- CommonFlow.expectPending("CardOfDayPhoto", chatId) {
        case BotPending.CardOfDayPhoto(mode, _, _, d) if d == description => d }
    } yield ()

  def selectCardOfDay(app: Routes[BotEnv, Response], chatId: Long, spreadId: UUID): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.selectCardOfDayRequest(chatId, spreadId)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      response <- app.runZIO(request)
      _ <- CommonFlow.expectStatusOk(response, "select card of day fail")
    } yield ()  
}
