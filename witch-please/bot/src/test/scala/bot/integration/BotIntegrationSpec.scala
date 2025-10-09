package bot.integration

import bot.api.BotApiRoutes
import bot.api.endpoints.*
import bot.domain.models.session.BotPendingAction
import bot.integration.flows.*
import bot.layers.BotEnv
import bot.layers.TestBotEnvLayer.testEnvLive
import bot.models.*
import bot.telegram.TestTelegramWebhook
import shared.infrastructure.services.clients.ZIOHttpClient
import shared.models.telegram.TelegramFile
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zio.http.*
import zio.json.*
import zio.test.*
import zio.test.TestAspect.sequential
import zio.*

object BotIntegrationSpec extends ZIOSpecDefault {
  final val resourcePath = "photos/test.png"
  final val cardCount = 2

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Bot API integration")(
    test("initialize test state") {
      for {
        photoId <- getPhoto

        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        _ <- ref.set(TestBotState(Some(photoId), None))
      } yield assertTrue(photoId.nonEmpty)
    },

    test("send start command") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        botSessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)
        chatId <- getChatId
        
        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        startRequest = TestTelegramWebhook.startRequest(chatId)
        request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), startRequest)
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
        botSessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)
        chatId <- getChatId

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        startRequest = TestTelegramWebhook.startRequest(chatId)
        request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), startRequest)
        response <- app.runZIO(request)

        session <- botSessionService.get(chatId)
      } yield assertTrue(
        response.status == Status.Ok,
        session.clientSecret == clientSecret,
        session.token.nonEmpty
      )
    },

    test("send project flow") {
      for {
        botSessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)
        chatId <- getChatId

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        _ <- ProjectFlow.startProject(app, chatId)
        _ <- ProjectFlow.projectName(app, chatId, "Test project")
        _ <- ProjectFlow.getProjects(app, chatId)

        session <- botSessionService.get(chatId)
      } yield assertTrue(
        session.projectId.nonEmpty,
        session.pending.isEmpty
      )
    },

    test("create spread command") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(new RuntimeException("photoId not set"))

        botSessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)
        chatId <- getChatId
        session <- botSessionService.get(chatId)
        projectId <- ZIO.fromOption(session.projectId).orElseFail(new RuntimeException("projectId not set"))

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
        _ <- SpreadFlow.startSpread(app, chatId)
        _ <- SpreadFlow.spreadTitle(app, chatId, "Test spread")
        _ <- SpreadFlow.spreadCardCount(app, chatId, cardCount)
        _ <- CommonFlow.sendPhoto(app, chatId, photoId)
        _ <- SpreadFlow.getSpreads(app, chatId, projectId)

        session <- botSessionService.get(chatId)
      } yield assertTrue(
        session.spreadId.nonEmpty,
        session.pending.isEmpty
      )
    },

    test("create card flow") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(new RuntimeException("photoId not set"))

        botSessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)
        chatId <- getChatId

        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)

        _ <- ZIO.foreachDiscard(1 to cardCount) { index =>
          for {
            _ <- CardFlow.startCard(app, chatId)
            _ <- CardFlow.cardIndex(app, chatId, index)
            _ <- CardFlow.cardDescription(app, chatId, s"Test card $index")
            _ <- CommonFlow.sendPhoto(app, chatId, photoId)
            _ <- CommonFlow.expectNoPending(chatId)
          } yield ()
        }

        session <- botSessionService.get(chatId)
        spreadProgress <- ZIO.fromOption(session.spreadProgress).orElseFail(new RuntimeException("progress not set"))
      } yield assertTrue(
        spreadProgress.total == cardCount,
        spreadProgress.createdCount == cardCount,
        spreadProgress.createdIndices == (0 until cardCount).toSet,
        session.pending.isEmpty
      )
    },

//    test("publish spread command") {
//      for {
//        botSessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)
//        chatId <- getChatId
//
//        app = ZioHttpInterpreter().toHttp(WebhookEndpoint.endpoints)
//        now <- Clock.instant
//        publishTime = now.plus(10.minute)
//        publishSpreadRequest = TestTelegramWebhook.publishSpreadRequest(chatId, publishTime)
//        request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), publishSpreadRequest)
//        response <- app.runZIO(request)
//
//        session <- botSessionService.get(chatId)
//      } yield assertTrue(
//        response.status == Status.Ok,
//        session.spreadId.isEmpty,
//        session.pending.isEmpty,
//        session.spreadProgress.isEmpty
//      )
//    }
  ).provideShared(
    Scope.default,
    testEnvLive,
    testBotStateLayer
  ) @@ sequential

  private val testBotStateLayer: ZLayer[Any, Nothing, Ref.Synchronized[TestBotState]] =
    ZLayer.fromZIO(Ref.Synchronized.make(TestBotState(None, None)))

  private def getPhoto: ZIO[BotEnv, Throwable, String] =
    for {
      fileStorageService <- ZIO.serviceWith[BotEnv](_.botService.fileStorageService)
      telegramApiService <- ZIO.serviceWith[BotEnv](_.botService.telegramApiService)
      photo <- fileStorageService.getResourcePhoto(resourcePath)
      telegramFile = TelegramFile(photo.fileName, photo.bytes)
      chatId <- getChatId
      photoId <- telegramApiService.sendPhoto(chatId, telegramFile)
    } yield photoId
    
  private def getChatId: ZIO[BotEnv, Throwable, Long] =
    for {
      telegramConfig <- ZIO.serviceWith[BotEnv](_.config.telegram)    
      chatId <- ZIO.fromOption(telegramConfig.chatId).orElseFail(RuntimeException("chatId not set"))
    } yield chatId
}
