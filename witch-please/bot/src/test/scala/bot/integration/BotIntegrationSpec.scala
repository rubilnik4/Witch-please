package bot.integration

import bot.api.BotApiRoutes
import bot.api.endpoints.*
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
    test("create user") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestBotState]]
        botSessionService <- ZIO.serviceWith[AppEnv](_.botService.botSessionService)
        chatId <- ZIO.serviceWith[AppEnv](_.appConfig.telegram.chatId)
        
        app = ZioHttpInterpreter().toHttp(List(WebhookEndpoint.postWebhookEndpoint))
        startRequest = TestTelegramWebhook.getStartRequest(chatId)
        request = ZIOHttpClient.getPostRequest(BotApiRoutes.postWebhookPath(""), startRequest)
        response <- app.runZIO(request)        
        
        clientSecret <- botSessionService.get(chatId).map(_.clientSecret)
      } yield assertTrue(
        response.status == Status.Ok,
        clientSecret.nonEmpty
      )
    },

//    test("auth user pre project role") {
//      for {
//        ref <- ZIO.service[Ref.Synchronized[TestProjectState]]
//        state <- ref.get
//        userId <- ZIO.fromOption(state.userId).orElseFail(TarotError.NotFound("userId not set"))
//
//        app = ZioHttpInterpreter().toHttp(List(AuthEndpoint.postAuthEndpoint))
//        authRequest = getAuthRequest(userId, clientSecret, None)
//        request = ZIOHttpClient.getPostRequest(TarotApiRoutes.tokenAuthPath(""), authRequest)
//        response <- app.runZIO(request)
//        auth <- ZIOHttpClient.getResponse[AuthResponse](response)
//
//        _ <- ref.set(TestProjectState(Some(userId), Some(auth.token), None))
//      } yield assertTrue(
//        auth.token.nonEmpty,
//        auth.role == Role.PreProject
//      )
//    },
//
//    test("create project") {
//      for {
//        ref <- ZIO.service[Ref.Synchronized[TestProjectState]]
//        state <- ref.get
//        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
//
//        app = ZioHttpInterpreter().toHttp(List(ProjectEndpoint.postProjectEndpoint))
//        projectRequest = getProjectCreateRequest
//        request = ZIOHttpClient.getAuthPostRequest(TarotApiRoutes.projectCreatePath(""), projectRequest, token)
//        response <- app.runZIO(request)
//        projectId <- ZIOHttpClient.getResponse[IdResponse](response).map(_.id)
//
//        ref <- ZIO.service[Ref.Synchronized[TestProjectState]]
//        _ <- ref.set(TestProjectState(state.userId, Some(token), Some(projectId)))
//      } yield assertTrue(projectId.toString.nonEmpty)
//    },
//
//    test("auth user admin role") {
//      for {
//        ref <- ZIO.service[Ref.Synchronized[TestProjectState]]
//        state <- ref.get
//        userId <- ZIO.fromOption(state.userId).orElseFail(TarotError.NotFound("userId not set"))
//        projectId <- ZIO.fromOption(state.projectId).orElseFail(TarotError.NotFound("projectId not set"))
//
//        app = ZioHttpInterpreter().toHttp(List(AuthEndpoint.postAuthEndpoint))
//        authRequest = getAuthRequest(userId, clientSecret, Some(projectId))
//        request = ZIOHttpClient.getPostRequest(TarotApiRoutes.tokenAuthPath(""), authRequest)
//        response <- app.runZIO(request)
//        auth <- ZIOHttpClient.getResponse[AuthResponse](response)
//      } yield assertTrue(
//        auth.token.nonEmpty,
//        auth.role == Role.Admin
//      )
//    }
  ).provideShared(
    Scope.default,
    testAppEnvLive,
    testBotStateLayer
  ) @@ sequential

  private val testBotStateLayer: ZLayer[Any, Nothing, Ref.Synchronized[TestBotState]] =
    ZLayer.fromZIO(Ref.Synchronized.make(TestBotState()))

//  private def getAuthRequest(userId: UUID, clientSecret: String, projectId: Option[UUID]) =
//    AuthRequest(
//      clientType = ClientType.Telegram,
//      userId = userId,
//      clientSecret = clientSecret,
//      projectId = projectId
//    )
//
//  private def getProjectCreateRequest =
//    ProjectCreateRequest(
//      name = "Test project"
//    )
}
