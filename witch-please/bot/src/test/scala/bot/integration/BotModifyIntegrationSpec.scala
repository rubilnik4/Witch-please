package bot.integration

import bot.api.BotApiRoutes
import bot.api.endpoints.*
import bot.domain.models.session.*
import bot.fixtures.BotTestFixtures
import bot.integration.BotIntegrationSpec.test
import bot.integration.flows.*
import bot.infrastructure.services.sessions.SessionRequire
import bot.integration.BotModifyKeepIntegrationSpec.cardsCount
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
        _ <- ChannelFlow.forwardChannelId(app, chatId, 11111)

        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        _ <- ref.set(TestBotState(Some(photoId), None))
      } yield assertTrue(photoId.nonEmpty)
    },

    test("update channel flow") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        chatId <- BotTestFixtures.getChatId
        channel <- SessionRequire.channel(chatId)

        channelMode = ChannelMode.Edit(channel.userChannelId)
        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        _ <- ChannelFlow.startChannel(app, chatId, channelMode)
        _ <- ChannelFlow.forwardChannelId(app, chatId, channelId)

        session <- SessionRequire.session(chatId)
      } yield assertTrue(
        session.channel.exists(_.channelId == channelId),
        session.pending.isEmpty
      )
    },

    test("create spread with cards flow and card of day") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- TestBotState.photoId(state)

        chatId <- BotTestFixtures.getChatId

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        spreadMode = SpreadMode.Create
        _ <- SpreadFlow.startSpread(app, chatId, spreadMode)
        _ <- SpreadFlow.spreadTitle(app, chatId, "Test spread")
        _ <- SpreadFlow.spreadCardCount(app, chatId, cardsCount)
        _ <- SpreadFlow.spreadDescription(app, chatId, "Test spread")
        _ <- CommonFlow.sendPhoto(app, chatId, photoId)
        _ <- ZIO.foreachDiscard(1 to cardsCount) { position =>
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
        spreadProgress.cardsCount == cardsCount,
        session.pending.isEmpty
      )
    },

    test("update spread") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- TestBotState.photoId(state)

        chatId <- BotTestFixtures.getChatId
        spread <- SessionRequire.spread(chatId)

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        spreadMode = SpreadMode.Edit(spread.spreadId)
        _ <- SpreadFlow.startSpread(app, chatId, spreadMode)
        _ <- SpreadFlow.spreadTitle(app, chatId, "Test spread")
        _ <- SpreadFlow.spreadCardCount(app, chatId, cardsCount)
        _ <- SpreadFlow.spreadDescription(app, chatId, "Test spread")
        _ <- CommonFlow.sendPhoto(app, chatId, photoId)

        session <- SessionRequire.session(chatId)
        spreadProgress <- SessionRequire.spreadProgress(chatId)
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
        photoId <- TestBotState.photoId(state)

        chatId <- BotTestFixtures.getChatId
        spreadProgress <- SessionRequire.spreadProgress(chatId)
        cardId <- ZIO.fromOption(spreadProgress.createdPositions.lastOption.map(_.cardId))
          .orElseFail(new RuntimeException("cardId not set"))

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        cardMode = CardMode.Edit(cardId)
        _ <- CardFlow.startCard(app, chatId, cardMode)
        _ <- CardFlow.cardTitle(app, chatId, "Test card")
        _ <- CardFlow.cardDescription(app, chatId, "Test card")
        _ <- CommonFlow.sendPhoto(app, chatId, photoId)

        session <- SessionRequire.session(chatId)
        spreadProgress <- SessionRequire.spreadProgress(chatId)
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
        photoId <- TestBotState.photoId(state)

        chatId <- BotTestFixtures.getChatId
        spread <- SessionRequire.spread(chatId)

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        _ <- CardOfDayFlow.selectCardOfDay(app, chatId, spread.spreadId)

        session <- SessionRequire.session(chatId)
      } yield assertTrue(
        session.cardOfDay.nonEmpty
      )
    },

    test("update card of day") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- TestBotState.photoId(state)

        chatId <- BotTestFixtures.getChatId
        cardOfDay <- SessionRequire.cardOfDay(chatId)

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        cardOfDayMode = CardOfDayMode.Edit(cardOfDay.cardOfDayId)
        spreadProgress <- SessionRequire.spreadProgress(chatId)
        cardPosition <- ZIO.fromOption(spreadProgress.createdPositions.headOption)
        _ <- CardOfDayFlow.startCardOfDay(app, chatId, cardOfDayMode)
        _ <- CardOfDayFlow.cardOfDayCardId(app, chatId, cardPosition)
        _ <- CardOfDayFlow.cardOfDayTitle(app, chatId, "Test card of day")
        _ <- CardOfDayFlow.cardOfDayDescription(app, chatId, "Test card of day")
        _ <- CommonFlow.sendPhoto(app, chatId, photoId)

        session <- SessionRequire.session(chatId)
      } yield assertTrue(
        session.cardOfDay.nonEmpty,
        session.pending.isEmpty
      )
    },

    test("select spread flow") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- TestBotState.photoId(state)

        chatId <- BotTestFixtures.getChatId
        spread <- SessionRequire.spread(chatId)

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        _ <- SpreadFlow.selectSpread(app, chatId, spread.spreadId)

        session <- SessionRequire.session(chatId)
        spreadProgress <- SessionRequire.spreadProgress(chatId)
      } yield assertTrue(
        spreadProgress.cardsCount == cardsCount,
        spreadProgress.createdPositions.size == cardsCount,
        spreadProgress.createdPositions.map(_.position) == (0 until cardsCount).toSet,
        session.pending.isEmpty
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
