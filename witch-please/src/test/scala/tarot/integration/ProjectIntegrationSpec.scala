package tarot.integration

import tarot.api.dto.tarot.authorize.*
import tarot.api.dto.tarot.users.*
import tarot.api.endpoints.ApiPath
import tarot.domain.models.TarotError
import tarot.domain.models.authorize.ClientType
import tarot.domain.models.authorize.Role.PreProject
import tarot.domain.models.contracts.TarotChannelType
import tarot.infrastructure.services.clients.ZIOHttpClient
import tarot.layers.TestAppEnvLayer.testAppEnvLive
import tarot.layers.{AppEnv, TestServerLayer}
import tarot.models.{TestProjectState, TestSpreadState}
import zio.*
import zio.http.*
import zio.json.*
import zio.test.*
import zio.test.TestAspect.sequential

import java.util.UUID

object ProjectIntegrationSpec extends ZIOSpecDefault {
  private val clientId = "123456789"
  private val clientSecret = "test-secret-token"

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Project API integration")(
    test("create user") {
      for {
        projectConfig <- ZIO.serviceWith[AppEnv](_.appConfig.project)
        userUrl = createUserUrl(projectConfig.serverUrl)
        userRequest = userCreateRequest(clientId, clientSecret)
        response <- ZIOHttpClient.sendPostAsString[UserCreateRequest](userUrl, userRequest)

        userId <- ZIO
          .attempt(UUID.fromString(response))
          .orElseFail(TarotError.ParsingError("UUID", s"Invalid user UUID returned: $response"))

        ref <- ZIO.service[Ref.Synchronized[TestProjectState]]
        _ <- ref.set(TestProjectState(Some(userId)))
      } yield assertTrue(userId.toString.nonEmpty)
    },

    test("auth user") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestProjectState]]
        state <- ref.get
        userId <- ZIO.fromOption(state.userId).orElseFail(TarotError.NotFound("userId not set"))

        projectConfig <- ZIO.serviceWith[AppEnv](_.appConfig.project)
        authUrl = createAuthUrl(projectConfig.serverUrl)
        request = authRequest(userId, clientSecret, None)
        response <- ZIOHttpClient.sendPost[AuthRequest, AuthResponse](authUrl, request)

      } yield assertTrue(
        response.token.nonEmpty,
        response.role == PreProject
      )
    }
  ).provideShared(
    TestServer.layer,
    Client.default,
    TestServerLayer.serverConfig,
    Driver.default,
    Scope.default,
    testAppEnvLive,
    TestServerLayer.testServerLayer,
    testProjectStateLayer
  ) @@ sequential

  private val testProjectStateLayer: ZLayer[Any, Nothing, Ref.Synchronized[TestProjectState]] =
    ZLayer.fromZIO(Ref.Synchronized.make(TestProjectState(None)))

  private def userCreateRequest(clientId: String, clientSecret: String) =
    UserCreateRequest(
      clientId = clientId,
      clientSecret = clientSecret,
      name = "Test User"
    )

  private def authRequest(userId: UUID, clientSecret: String, projectId: Option[UUID]) =
    AuthRequest(
      clientType = ClientType.Telegram,
      userId = userId,
      clientSecret = clientSecret,
      projectId = projectId
    )

  private def createUserUrl(serverUrl: String) =
    val path = s"/api/${TarotChannelType.Telegram}/user"
    ApiPath.getRoutePath(serverUrl, path)

  private def createAuthUrl(serverUrl: String) =
    val path = s"/api/auth"
    ApiPath.getRoutePath(serverUrl, path)
}
