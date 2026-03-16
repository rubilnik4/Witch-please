package bot.integration

import bot.api.BotApiRoutes
import bot.api.endpoints.*
import bot.domain.models.session.*
import bot.fixtures.BotTestFixtures
import bot.infrastructure.services.sessions.SessionRequire
import bot.integration.BotIntegrationSpec.test
import bot.integration.flows.*
import bot.layers.TestBotEnvLayer
import bot.models.*
import bot.telegram.TestTelegramWebhook
import shared.infrastructure.services.clients.ZIOHttpClient
import shared.models.tarot.spreads.SpreadStatus
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zio.*
import zio.http.*
import zio.json.*
import zio.test.*
import zio.test.TestAspect.sequential

object BotDeleteIntegrationSpec extends ZIOSpecDefault {
  final val cardsCount = 2
  final val channelId = 54321

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Bot API delete integration")(
    test("initialize test state") {
      for {
        photoId <- BotTestFixtures.getPhoto
        chatId <- BotTestFixtures.getChatId

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        startRequest = TestTelegramWebhook.startRequest(chatId)
        request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), startRequest)
        _ <- app.runZIO(request)

        startAuthorRequest = TestTelegramWebhook.startAuthorRequest(chatId)
        request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), startAuthorRequest)
        _ <- app.runZIO(request)

        channelMode = ChannelMode.Create
        _ <- ChannelFlow.startChannel(app, chatId, channelMode)
        _ <- ChannelFlow.forwardChannelId(app, chatId, 11111)

        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        _ <- ref.set(TestBotState(Some(photoId), None))
      } yield assertTrue(photoId.nonEmpty)
    },

    test("create spread with cards flow and card of day") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- TestBotState.photoId(state)

        chatId <- BotTestFixtures.getChatId

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        spreadMode = SpreadMode.Create
        spreadCardsCount = cardsCount + 1
        _ <- SpreadFlow.startSpread(app, chatId, spreadMode)
        _ <- SpreadFlow.spreadTitle(app, chatId, "Test spread")
        _ <- SpreadFlow.spreadCardCount(app, chatId, spreadCardsCount)
        _ <- SpreadFlow.spreadDescription(app, chatId, "Test spread")
        _ <- CommonFlow.sendPhoto(app, chatId, photoId)
        _ <- ZIO.foreachDiscard(1 to spreadCardsCount) { position =>
          val cardMode = CardMode.Create(position - 1)
          for {
            _ <- CardFlow.startCard(app, chatId, cardMode)
            _ <- CardFlow.cardTitle(app, chatId, "Test card")
            _ <- CardFlow.cardDescription(app, chatId, "Test card")
            _ <- CommonFlow.sendPhoto(app, chatId, photoId)
          } yield ()
        }
        cardOfDayMode = CardOfDayMode.Create
        spreadProgress <- SessionRequire.spreadProgress(chatId)
        cardPosition <- ZIO.fromOption(spreadProgress.createdPositions.headOption)
          .orElseFail(new RuntimeException("cardId not set"))
        _ <- CardOfDayFlow.startCardOfDay(app, chatId, cardOfDayMode)
        _ <- CardOfDayFlow.cardOfDayCardId(app, chatId, cardPosition)
        _ <- CardOfDayFlow.cardOfDayTitle(app, chatId, "Test card of day")
        _ <- CardOfDayFlow.cardOfDayDescription(app, chatId, "Test card of day")
        _ <- CommonFlow.sendPhoto(app, chatId, photoId)

        session <- SessionRequire.session(chatId)
        spreadProgress <- SessionRequire.spreadProgress(chatId)
      } yield assertTrue(
        session.spread.exists(_.status == SpreadStatus.Draft),
        spreadProgress.cardsCount == spreadCardsCount,
        session.pending.isEmpty
      )
    },

    test("delete card") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- TestBotState.photoId(state)

        chatId <- BotTestFixtures.getChatId
        spreadProgress <- SessionRequire.spreadProgress(chatId)
        cardId <- ZIO.fromOption(spreadProgress.createdPositions.lastOption.map(_.cardId))
          .orElseFail(new RuntimeException("cardId not set"))

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        postRequest = TestTelegramWebhook.deleteCardRequest(chatId, cardId)
        request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
        _ <- app.runZIO(request)

        session <- SessionRequire.session(chatId)
        spreadProgress <- SessionRequire.spreadProgress(chatId)
      } yield assertTrue(
        spreadProgress.cardsCount == cardsCount + 1,
        spreadProgress.createdPositions.size == cardsCount,
        !spreadProgress.createdPositions.exists(_.cardId == cardId),
        session.card.isEmpty,
        session.pending.isEmpty
      )
    },

    test("delete card of day") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- TestBotState.photoId(state)

        chatId <- BotTestFixtures.getChatId
        cardOfDay <- SessionRequire.cardOfDay(chatId)

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        postRequest = TestTelegramWebhook.deleteCardOfDayRequest(chatId, cardOfDay.cardOfDayId)
        request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
        _ <- app.runZIO(request)

        session <- SessionRequire.session(chatId)
        spreadProgress <- SessionRequire.spreadProgress(chatId)
      } yield assertTrue(
        session.cardOfDay.isEmpty,
        session.pending.isEmpty
      )
    },

    test("delete spread") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- TestBotState.photoId(state)

        chatId <- BotTestFixtures.getChatId
        spread <- SessionRequire.spread(chatId)

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        postRequest = TestTelegramWebhook.deleteSpreadRequest(chatId, spread.spreadId)
        request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
        _ <- app.runZIO(request)

        session <- SessionRequire.session(chatId)
      } yield assertTrue(
        session.spread.isEmpty,
        session.spreadProgress.isEmpty,
        session.cardOfDay.isEmpty,
        session.card.isEmpty
      )
    },
  ).provideShared(
    Scope.default,
    TestBotEnvLayer.testEnvLive,
    testBotStateLayer
  ) @@ sequential

  private val testBotStateLayer: ZLayer[Any, Nothing, Ref.Synchronized[TestBotState]] =
    ZLayer.fromZIO(Ref.Synchronized.make(TestBotState(None, None)))
}
