package bot.integration

import bot.api.BotApiRoutes
import bot.api.endpoints.*
import bot.domain.models.session.BotPendingAction
import bot.layers.BotEnv
import bot.layers.TestAppEnvLayer.testAppEnvLive
import bot.models.*
import bot.telegram.TestTelegramWebhook
import shared.infrastructure.services.clients.ZIOHttpClient
import shared.models.telegram.TelegramFile
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.ztapir.*
import zio.http.*
import zio.json.*
import zio.test.*
import zio.test.TestAspect.sequential
import zio.{Ref, Scope, ZIO, ZLayer}

object BotIntegrationSpec extends ZIOSpecDefault {
  final val resourcePath = "photos/test.png"

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Bot API integration")(
    test("initialize test state") {
      for {
        photoId <- getPhoto

        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        _ <- ref.set(TestBotState(Some(photoId)))
      } yield assertTrue(photoId.nonEmpty)
    },

    test("send start command") {
      for {
        botSessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)
        chatId <- ZIO.serviceWith[BotEnv](_.appConfig.telegram.chatId)
        
        app = ZioHttpInterpreter().toHttp(List(WebhookEndpoint.postWebhookEndpoint))
        startRequest = TestTelegramWebhook.startRequest(chatId)
        request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), startRequest)
        response <- app.runZIO(request)        
        
        session <- botSessionService.get(chatId)
      } yield assertTrue(
        response.status == Status.Ok,
        session.clientSecret.nonEmpty,
        session.token.nonEmpty
      )
    },

    test("send project command") {
      for {
        botSessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)
        chatId <- ZIO.serviceWith[BotEnv](_.appConfig.telegram.chatId)

        app = ZioHttpInterpreter().toHttp(List(WebhookEndpoint.postWebhookEndpoint))
        createProjectRequest = TestTelegramWebhook.createProjectRequest(chatId)
        request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), createProjectRequest)
        response <- app.runZIO(request)

        session <- botSessionService.get(chatId)
      } yield assertTrue(
        response.status == Status.Ok,
        session.projectId.nonEmpty,
        session.token.nonEmpty
      )
    },

    test("create spread command") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(new RuntimeException("photoId not set"))

        botSessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)
        chatId <- ZIO.serviceWith[BotEnv](_.appConfig.telegram.chatId)

        app = ZioHttpInterpreter().toHttp(List(WebhookEndpoint.postWebhookEndpoint))

        cardCount = 2
        createSpreadRequest = TestTelegramWebhook.createSpreadRequest(chatId, cardCount)
        pendingRequest = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), createSpreadRequest)
        _ <- app.runZIO(pendingRequest)

        photoRequest = TestTelegramWebhook.photoRequest(chatId, photoId)
        request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), photoRequest)
        response <- app.runZIO(request)

        session <- botSessionService.get(chatId)
      } yield assertTrue(
        response.status == Status.Ok,
        session.spreadId.nonEmpty
      )
    }
  ).provideShared(
    Scope.default,
    testAppEnvLive,
    testBotStateLayer
  ) @@ sequential

  private val testBotStateLayer: ZLayer[Any, Nothing, Ref.Synchronized[TestBotState]] =
    ZLayer.fromZIO(Ref.Synchronized.make(TestBotState(None)))

  private def getPhoto: ZIO[BotEnv, Throwable, String] =
    for {
      fileStorageService <- ZIO.serviceWith[BotEnv](_.botService.fileStorageService)
      telegramApiService <- ZIO.serviceWith[BotEnv](_.botService.telegramApiService)
      telegramConfig <- ZIO.serviceWith[BotEnv](_.appConfig.telegram)
      photo <- fileStorageService.getResourcePhoto(resourcePath)
      telegramFile = TelegramFile(photo.fileName, photo.bytes)
      photoId <- telegramApiService.sendPhoto(telegramConfig.chatId, telegramFile)
    } yield photoId
}
