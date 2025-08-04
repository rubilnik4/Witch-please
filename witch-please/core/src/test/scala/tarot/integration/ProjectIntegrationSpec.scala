package tarot.integration

import tarot.api.dto.common.IdResponse
import tarot.api.dto.tarot.authorize.*
import tarot.api.dto.tarot.projects.ProjectCreateRequest
import tarot.api.dto.tarot.users.*
import tarot.api.endpoints.ApiPath
import tarot.domain.models.TarotError
import tarot.domain.models.authorize.{ClientType, Role}
import tarot.domain.models.contracts.TarotChannelType
import tarot.infrastructure.services.clients.ZIOHttpClient
import tarot.layers.TestAppEnvLayer.testAppEnvLive
import tarot.layers.{AppEnv, TestServerLayer}
import tarot.models.TestProjectState
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
        response <- ZIOHttpClient.sendPost[UserCreateRequest, IdResponse](userUrl, userRequest)
        userId = response.id

        ref <- ZIO.service[Ref.Synchronized[TestProjectState]]
        _ <- ref.set(TestProjectState(Some(userId), None, None))
      } yield assertTrue(userId.toString.nonEmpty)
    },

    test("auth user pre project role") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestProjectState]]
        state <- ref.get
        userId <- ZIO.fromOption(state.userId).orElseFail(TarotError.NotFound("userId not set"))

        projectConfig <- ZIO.serviceWith[AppEnv](_.appConfig.project)
        authUrl = createAuthUrl(projectConfig.serverUrl)
        request = authRequest(userId, clientSecret, None)
        response <- ZIOHttpClient.sendPost[AuthRequest, AuthResponse](authUrl, request)

        _ <- ref.set(TestProjectState(Some(userId), Some(response.token), None))
      } yield assertTrue(
        response.token.nonEmpty,
        response.role == Role.PreProject
      )
    },

    test("create project") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestProjectState]]
        state <- ref.get
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        projectConfig <- ZIO.serviceWith[AppEnv](_.appConfig.project)
        projectUrl = createProjectUrl(projectConfig.serverUrl)
        request = projectRequest()
        response <- ZIOHttpClient.sendPostAuth[ProjectCreateRequest, IdResponse](projectUrl, request, token)
        projectId = response.id

        ref <- ZIO.service[Ref.Synchronized[TestProjectState]]
        _ <- ref.set(TestProjectState(state.userId, Some(token), Some(projectId)))
      } yield assertTrue(
        projectId.toString.nonEmpty
      )
    },

    test("auth user admin role") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestProjectState]]
        state <- ref.get
        userId <- ZIO.fromOption(state.userId).orElseFail(TarotError.NotFound("userId not set"))
        projectId <- ZIO.fromOption(state.projectId).orElseFail(TarotError.NotFound("projectId not set"))

        projectConfig <- ZIO.serviceWith[AppEnv](_.appConfig.project)
        authUrl = createAuthUrl(projectConfig.serverUrl)
        request = authRequest(userId, clientSecret, Some(projectId))
        response <- ZIOHttpClient.sendPost[AuthRequest, AuthResponse](authUrl, request)
      } yield assertTrue(
        response.token.nonEmpty,
        response.role == Role.Admin
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
    ZLayer.fromZIO(Ref.Synchronized.make(TestProjectState(None, None, None)))

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

  private def projectRequest() =
    ProjectCreateRequest(
      name = "Test project"
    )

  private def createUserUrl(serverUrl: String) =
    val path = s"/api/${TarotChannelType.Telegram}/user"
    ApiPath.getRoutePath(serverUrl, path)

  private def createAuthUrl(serverUrl: String) =
    val path = s"/api/auth"
    ApiPath.getRoutePath(serverUrl, path)

  private def createProjectUrl(serverUrl: String) =
    val path = s"/api/project"
    ApiPath.getRoutePath(serverUrl, path)
}
