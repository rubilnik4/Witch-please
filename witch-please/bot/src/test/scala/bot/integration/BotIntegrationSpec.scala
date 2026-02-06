package bot.integration

import bot.api.BotApiRoutes
import bot.api.endpoints.*
import bot.domain.models.session.*
import bot.fixtures.BotTestFixtures
import bot.integration.flows.*
import bot.layers.{BotEnv, TestBotEnvLayer}
import bot.models.*
import bot.telegram.TestTelegramWebhook
import shared.infrastructure.services.clients.ZIOHttpClient
import shared.infrastructure.services.common.DateTimeService
import shared.models.tarot.spreads.SpreadStatus
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zio.*
import zio.http.*
import zio.json.*
import zio.test.*
import zio.test.TestAspect.sequential

object BotIntegrationSpec extends ZIOSpecDefault {
  final val resourcePath = "photos/test.png"
  final val cardsCount = 2
  final val channelId = 54321

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Bot API integration")(
    test("initialize test state") {
      for {
        photoId <- BotTestFixtures.getPhoto

        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        _ <- ref.set(TestBotState(Some(photoId), None))
      } yield assertTrue(photoId.nonEmpty)
    },

    test("send start command") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        chatId <- BotTestFixtures.getChatId
        
        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        startRequest = TestTelegramWebhook.startRequest(chatId)
        request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), startRequest)
        response <- app.runZIO(request)
      } yield assertTrue(response.status == Status.Ok)
    },

    test("send start author command") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
        chatId <- BotTestFixtures.getChatId

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        startAuthorRequest = TestTelegramWebhook.startAuthorRequest(chatId)
        request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), startAuthorRequest)
        response <- app.runZIO(request)

        session <- botSessionService.get(chatId)
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        _ <- ref.set(TestBotState(state.photoId, Some(session.clientSecret)))
      } yield assertTrue(
        response.status == Status.Ok,
        session.clientSecret.nonEmpty,
        session.token.nonEmpty
      )
    },

    test("send restart command") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        clientSecret <- ZIO.fromOption(state.clientSecret).orElseFail(new RuntimeException("clientSecret not set"))
        botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
        chatId <- BotTestFixtures.getChatId

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        startAuthorRequest = TestTelegramWebhook.startAuthorRequest(chatId)
        request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), startAuthorRequest)
        response <- app.runZIO(request)

        session <- botSessionService.get(chatId)
      } yield assertTrue(
        response.status == Status.Ok,
        session.clientSecret == clientSecret,
        session.token.nonEmpty
      )
    },

    test("create channel flow") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
        chatId <- BotTestFixtures.getChatId
        channelMode = ChannelMode.Create

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        _ <- ChannelFlow.startChannel(app, chatId, channelMode)
        _ <- ChannelFlow.channelChannelId(app, chatId, channelId)

        session <- botSessionService.get(chatId)
      } yield assertTrue(
        session.channel.exists(_.channelId == channelId),
        session.pending.isEmpty
      )
    },

    test("create spread flow") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(new RuntimeException("photoId not set"))

        botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
        chatId <- BotTestFixtures.getChatId
        session <- botSessionService.get(chatId)
        spreadMode = SpreadMode.Create
          
        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)        
        _ <- SpreadFlow.startSpread(app, chatId, spreadMode)
        _ <- SpreadFlow.spreadTitle(app, chatId, "Test spread")
        _ <- SpreadFlow.spreadCardCount(app, chatId, cardsCount)
        _ <- SpreadFlow.spreadDescription(app, chatId, "Test spread")
        _ <- CommonFlow.sendPhoto(app, chatId, photoId)

        session <- botSessionService.get(chatId)
      } yield assertTrue(
        session.spread.exists(_.status == SpreadStatus.Draft),
        session.pending.isEmpty
      )
    },

    test("create card flow") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(new RuntimeException("photoId not set"))

        botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
        chatId <- BotTestFixtures.getChatId
        session <- botSessionService.get(chatId)
        spread <- ZIO.fromOption(session.spread).orElseFail(new RuntimeException("spreadId not set"))

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        _ <- ZIO.foreachDiscard(1 to cardsCount) { position =>
          val cardMode = CardMode.Create(position - 1)
          for {
            _ <- CardFlow.startCard(app, chatId, cardMode)
            _ <- CardFlow.cardTitle(app, chatId, "Test card")
            _ <- CardFlow.cardDescription(app, chatId, "Test card")
            _ <- CommonFlow.sendPhoto(app, chatId, photoId)
            _ <- CommonFlow.expectNoPending(chatId)
          } yield ()
        }

        session <- botSessionService.get(chatId)
        spreadProgress <- ZIO.fromOption(session.spreadProgress).orElseFail(new RuntimeException("progress not set"))
      } yield assertTrue(
        spreadProgress.cardsCount == cardsCount,
        spreadProgress.createdPositions.size == cardsCount,
        spreadProgress.createdPositions.map(_.position) == (0 until cardsCount).toSet,
        session.pending.isEmpty
      )
    },

    test("create card of day flow") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(new RuntimeException("photoId not set"))

        botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
        chatId <- BotTestFixtures.getChatId
        session <- botSessionService.get(chatId)
        spreadProgress <- ZIO.fromOption(session.spreadProgress).orElseFail(new RuntimeException("SpreadProgress not set"))
        cardPosition = spreadProgress.createdPositions.head
        cardOfDayMode = CardOfDayMode.Create

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        _ <- CardOfDayFlow.startCardOfDay(app, chatId, cardOfDayMode)
        _ <- CardOfDayFlow.cardOfDayCardId(app, chatId, cardPosition)
        _ <- CardOfDayFlow.cardOfDayTitle(app, chatId, "Test card of day")
        _ <- CardOfDayFlow.cardOfDayDescription(app, chatId, "Test card of day")
        _ <- CommonFlow.sendPhoto(app, chatId, photoId)
        _ <- CommonFlow.expectNoPending(chatId)

        session <- botSessionService.get(chatId)
      } yield assertTrue(
        session.cardOfDayId.nonEmpty,
        session.pending.isEmpty
      )
    },

    test("publish spread flow") {
      for {
        botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
        tarotApiService <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
        chatId <- BotTestFixtures.getChatId
        session <- botSessionService.get(chatId)
        token <- ZIO.fromOption(session.token).orElseFail(new RuntimeException("token not set"))
        spread <- ZIO.fromOption(session.spread).orElseFail(new RuntimeException("spreadId not set"))

        now <- DateTimeService.getDateTimeNow
        publishTime = now.plus(1.day)
        cardOdDayDelay = 1.hour

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        _ <- PublishFlow.startPublish(app, chatId, spread.spreadId)
        _ <- PublishFlow.selectMonth(app, chatId, publishTime)
        _ <- PublishFlow.selectDate(app, chatId, publishTime)
        _ <- PublishFlow.selectTime(app, chatId, publishTime)
        _ <- PublishFlow.selectCardOdDayDelay(app, chatId, cardOdDayDelay)
        _ <- PublishFlow.confirm(app, chatId)

        session <- botSessionService.get(chatId)
        spread <- tarotApiService.getSpread(spread.spreadId, token)
      } yield assertTrue(
        session.spread.exists(_.status == SpreadStatus.Scheduled),
        spread.status == SpreadStatus.Scheduled,
        spread.scheduledAt.contains(publishTime)
      )
    }
  ).provideShared(
    Scope.default,
    TestBotEnvLayer.testEnvLive,
    testBotStateLayer
  ) @@ sequential

  private val testBotStateLayer: ZLayer[Any, Nothing, Ref.Synchronized[TestBotState]] =
    ZLayer.fromZIO(Ref.Synchronized.make(TestBotState(None, None)))
}
