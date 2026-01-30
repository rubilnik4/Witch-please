package bot.integration

import bot.api.BotApiRoutes
import bot.api.endpoints.*
import bot.domain.models.session.*
import bot.fixtures.BotTestFixtures
import bot.integration.BotIntegrationSpec.test
import bot.integration.flows.*
import bot.layers.{BotEnv, TestBotEnvLayer}
import bot.models.*
import bot.telegram.TestTelegramWebhook
import shared.infrastructure.services.clients.ZIOHttpClient
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zio.*
import zio.http.*
import zio.json.*
import zio.test.*
import zio.test.TestAspect.sequential

object BotModifyIntegrationSpec extends ZIOSpecDefault {
  final val cardsCount = 2
  final val channelId = 54321

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Bot API modify integration")(
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
        _ <- ChannelFlow.channelChannelId(app, chatId, 11111)

        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        _ <- ref.set(TestBotState(Some(photoId), None))
      } yield assertTrue(photoId.nonEmpty)
    },

    test("update channel flow") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
        chatId <- BotTestFixtures.getChatId
        session <- botSessionService.get(chatId)
        channel <- ZIO.fromOption(session.channel).orElseFail(new RuntimeException("channel not set"))

        channelMode = ChannelMode.Edit(channel.userChannelId)
        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        _ <- ChannelFlow.startChannel(app, chatId, channelMode)
        _ <- ChannelFlow.channelChannelId(app, chatId, channelId)

        session <- botSessionService.get(chatId)
      } yield assertTrue(
        session.channel.exists(_.channelId == channelId),
        session.pending.isEmpty
      )
    },

    test("create spread with cards flow and card of day") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(new RuntimeException("photoId not set"))

        botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
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
        session <- botSessionService.get(chatId)
        cardPosition <- ZIO.fromOption(session.spreadProgress.flatMap(_.createdPositions.headOption))
          .orElseFail(new RuntimeException("cardId not set"))
        _ <- CardOfDayFlow.startCardOfDay(app, chatId, cardOfDayMode)
        _ <- CardOfDayFlow.cardOfDayCardId(app, chatId, cardPosition)
        _ <- CardOfDayFlow.cardOfDayTitle(app, chatId, "Test card of day")
        _ <- CardOfDayFlow.cardOfDayDescription(app, chatId, "Test card of day")
        _ <- CommonFlow.sendPhoto(app, chatId, photoId)

        session <- botSessionService.get(chatId)
      } yield assertTrue(
        session.spreadId.nonEmpty,
        session.spreadProgress.exists(_.cardsCount == spreadCardsCount),
        session.pending.isEmpty
      )
    },

    test("delete card") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(new RuntimeException("photoId not set"))

        botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
        chatId <- BotTestFixtures.getChatId
        session <- botSessionService.get(chatId)
        cardId <- ZIO.fromOption(session.spreadProgress.flatMap(_.createdPositions.lastOption.map(_.cardId)))
          .orElseFail(new RuntimeException("cardId not set"))

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        postRequest = TestTelegramWebhook.deleteCardRequest(chatId, cardId)
        request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
        _ <- app.runZIO(request)

        session <- botSessionService.get(chatId)
        spreadProgress <- ZIO.fromOption(session.spreadProgress).orElseFail(new RuntimeException("progress not set"))
      } yield assertTrue(
        spreadProgress.cardsCount == cardsCount + 1,
        spreadProgress.createdPositions.size == cardsCount,
        !spreadProgress.createdPositions.exists(_.cardId == cardId),
        session.cardId.isEmpty,
        session.pending.isEmpty
      )
    },

    test("update spread") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(new RuntimeException("photoId not set"))

        botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
        chatId <- BotTestFixtures.getChatId
        session <- botSessionService.get(chatId)
        spreadId <- ZIO.fromOption(session.spreadId).orElseFail(new RuntimeException("spreadId not set"))

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        spreadMode = SpreadMode.Edit(spreadId)
        _ <- SpreadFlow.startSpread(app, chatId, spreadMode)
        _ <- SpreadFlow.spreadTitle(app, chatId, "Test spread")
        _ <- SpreadFlow.spreadCardCount(app, chatId, cardsCount)
        _ <- SpreadFlow.spreadDescription(app, chatId, "Test spread")
        _ <- CommonFlow.sendPhoto(app, chatId, photoId)

        session <- botSessionService.get(chatId)
        spreadProgress <- ZIO.fromOption(session.spreadProgress).orElseFail(new RuntimeException("progress not set"))
      } yield assertTrue(
        spreadProgress.cardsCount == cardsCount,
        spreadProgress.createdPositions.size == cardsCount,
        session.pending.isEmpty
      )
    },

    test("update card") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(new RuntimeException("photoId not set"))

        botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
        chatId <- BotTestFixtures.getChatId
        session <- botSessionService.get(chatId)
        cardId <- ZIO.fromOption(session.spreadProgress.flatMap(_.createdPositions.lastOption.map(_.cardId)))
          .orElseFail(new RuntimeException("cardId not set"))

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        cardMode = CardMode.Edit(cardId)
        _ <- CardFlow.startCard(app, chatId, cardMode)
        _ <- CardFlow.cardTitle(app, chatId, "Test card")
        _ <- CardFlow.cardDescription(app, chatId, "Test card")
        _ <- CommonFlow.sendPhoto(app, chatId, photoId)
        _ <- CommonFlow.expectNoPending(chatId)

        session <- botSessionService.get(chatId)
        spreadProgress <- ZIO.fromOption(session.spreadProgress).orElseFail(new RuntimeException("progress not set"))
      } yield assertTrue(
        spreadProgress.cardsCount == cardsCount,
        spreadProgress.createdPositions.size == cardsCount,
        spreadProgress.createdPositions.map(_.position) == (0 until cardsCount).toSet,
        session.pending.isEmpty
      )
    },

    test("select card of day flow") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(new RuntimeException("photoId not set"))

        botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
        chatId <- BotTestFixtures.getChatId
        session <- botSessionService.get(chatId)
        spreadId <- ZIO.fromOption(session.spreadId).orElseFail(new RuntimeException("spreadId not set"))

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        _ <- CardOfDayFlow.selectCardOfDay(app, chatId, spreadId)

        session <- botSessionService.get(chatId)
      } yield assertTrue(
        session.cardOfDayId.nonEmpty
      )
    },

    test("update card of day") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(new RuntimeException("photoId not set"))

        botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
        chatId <- BotTestFixtures.getChatId
        session <- botSessionService.get(chatId)
        cardOfDayId <- ZIO.fromOption(session.cardOfDayId).orElseFail(new RuntimeException("cardOfDayId not set"))

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        cardOfDayMode = CardOfDayMode.Edit(cardOfDayId)
        cardPosition <- ZIO.fromOption(session.spreadProgress.flatMap(_.createdPositions.headOption))
        _ <- CardOfDayFlow.startCardOfDay(app, chatId, cardOfDayMode)
        _ <- CardOfDayFlow.cardOfDayCardId(app, chatId, cardPosition)
        _ <- CardOfDayFlow.cardOfDayTitle(app, chatId, "Test card of day")
        _ <- CardOfDayFlow.cardOfDayDescription(app, chatId, "Test card of day")
        _ <- CommonFlow.sendPhoto(app, chatId, photoId)

        session <- botSessionService.get(chatId)
      } yield assertTrue(
        session.cardOfDayId.nonEmpty,
        session.pending.isEmpty
      )
    },

    test("delete card of day") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(new RuntimeException("photoId not set"))

        botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
        chatId <- BotTestFixtures.getChatId
        session <- botSessionService.get(chatId)
        cardOfDayId <- ZIO.fromOption(session.cardOfDayId).orElseFail(new RuntimeException("cardOfDayId not set"))

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        postRequest = TestTelegramWebhook.deleteCardOfDayRequest(chatId, cardOfDayId)
        request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
        _ <- app.runZIO(request)

        session <- botSessionService.get(chatId)
        spreadProgress <- ZIO.fromOption(session.spreadProgress).orElseFail(new RuntimeException("progress not set"))
      } yield assertTrue(
        session.cardOfDayId.isEmpty,
        session.pending.isEmpty
      )
    },

    test("select spread flow") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(new RuntimeException("photoId not set"))

        botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
        chatId <- BotTestFixtures.getChatId
        session <- botSessionService.get(chatId)
        spreadId <- ZIO.fromOption(session.spreadId).orElseFail(new RuntimeException("spreadId not set"))

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        _ <- SpreadFlow.selectSpread(app, chatId, spreadId)

        session <- botSessionService.get(chatId)
        spreadProgress <- ZIO.fromOption(session.spreadProgress).orElseFail(new RuntimeException("progress not set"))
      } yield assertTrue(
        spreadProgress.cardsCount == cardsCount,
        spreadProgress.createdPositions.size == cardsCount,
        spreadProgress.createdPositions.map(_.position) == (0 until cardsCount).toSet,
        session.pending.isEmpty
      )
    },

    test("delete spread") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(new RuntimeException("photoId not set"))

        botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
        chatId <- BotTestFixtures.getChatId
        session <- botSessionService.get(chatId)
        spreadId <- ZIO.fromOption(session.spreadId).orElseFail(new RuntimeException("spreadId not set"))

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        postRequest = TestTelegramWebhook.deleteSpreadRequest(chatId, spreadId)
        request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
        _ <- app.runZIO(request)

        session <- botSessionService.get(chatId)
      } yield assertTrue(
        session.spreadId.isEmpty,
        session.spreadProgress.isEmpty,
        session.cardOfDayId.isEmpty,
        session.cardId.isEmpty
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
