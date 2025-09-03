package bot.integration

import bot.api.BotApiRoutes
import bot.api.endpoints.*
import bot.domain.models.session.BotPendingAction
import bot.layers.AppEnv
import bot.layers.TestAppEnvLayer.testAppEnvLive
import bot.models.*
import bot.telegram.TestTelegramWebhook
import shared.infrastructure.services.clients.ZIOHttpClient
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.ztapir.*
import zio.http.*
import zio.json.*
import zio.test.*
import zio.test.TestAspect.sequential
import zio.{Ref, Scope, ZIO, ZLayer}

object BotIntegrationSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Bot API integration")(
    test("send start command") {
      for {
        botSessionService <- ZIO.serviceWith[AppEnv](_.botService.botSessionService)
        chatId <- ZIO.serviceWith[AppEnv](_.appConfig.telegram.chatId)
        
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
        botSessionService <- ZIO.serviceWith[AppEnv](_.botService.botSessionService)
        chatId <- ZIO.serviceWith[AppEnv](_.appConfig.telegram.chatId)

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
        botSessionService <- ZIO.serviceWith[AppEnv](_.botService.botSessionService)
        chatId <- ZIO.serviceWith[AppEnv](_.appConfig.telegram.chatId)

        app = ZioHttpInterpreter().toHttp(List(WebhookEndpoint.postWebhookEndpoint))
        cardCount = 2
        createSpreadRequest = TestTelegramWebhook.createSpreadRequest(chatId, cardCount)
        request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), createSpreadRequest)
        response <- app.runZIO(request)

        session <- botSessionService.get(chatId)
        pending <- ZIO.fromOption(session.pending).orElseFail(RuntimeException("session pending not found"))
      } yield assertTrue(
        response.status == Status.Ok,
        session.spreadId.nonEmpty,
        pending == BotPendingAction.SpreadCover
      )
    },
  ).provideShared(
    Scope.default,
    testAppEnvLive
  ) @@ sequential

  private val testBotStateLayer: ZLayer[Any, Nothing, Ref.Synchronized[TestBotState]] =
    ZLayer.fromZIO(Ref.Synchronized.make(TestBotState()))
}
