package tarot.integration

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.authorize.{AuthRequest, AuthResponse}
import shared.api.dto.tarot.common.IdResponse
import shared.api.dto.tarot.projects.ProjectCreateRequest
import shared.api.dto.tarot.users.*
import shared.infrastructure.services.clients.ZIOHttpClient
import shared.models.tarot.authorize.*
import shared.models.tarot.contracts.*
import tarot.api.dto.tarot.authorize.*
import tarot.api.dto.tarot.users.*
import tarot.api.endpoints.*
import tarot.domain.models.TarotError
import tarot.layers.TestTarotEnvLayer
import tarot.layers.{TarotEnv, TestTarotEnvLayer}
import tarot.models.TestProjectState
import zio.*
import zio.test.*
import zio.json.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.ztapir.*
import sttp.model.Method
import sttp.tapir.json.zio.jsonBody
import tarot.data.UserData
import zio.http.{Body, Driver, Request, URL}
import zio.test.TestAspect.sequential
import zio.http.*

import java.util.UUID

object ProjectIntegrationSpec extends ZIOSpecDefault {
  private final val clientId = UserData.generateClientId()
  private final val clientSecret = UserData.generateClientSecret()

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Project API integration")(
    test("create user") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestProjectState]]

        app = ZioHttpInterpreter().toHttp(List(UserEndpoint.postUserEndpoint))
        userRequest = getUserCreateRequest(clientId, clientSecret)
        request = ZIOHttpClient.postRequest(TarotApiRoutes.userCreatePath(""), userRequest)
        response <- app.runZIO(request)
        userId <- ZIOHttpClient.getResponse[IdResponse](response).map(_.id)

        _ <- ref.set(TestProjectState(Some(userId), None, None))
      } yield assertTrue(userId.toString.nonEmpty)
    },

    test("auth user pre project role") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestProjectState]]
        state <- ref.get
        userId <- ZIO.fromOption(state.userId).orElseFail(TarotError.NotFound("userId not set"))

        app = ZioHttpInterpreter().toHttp(List(AuthEndpoint.postAuthEndpoint))
        authRequest = getAuthRequest(userId, clientSecret, None)
        request = ZIOHttpClient.postRequest(TarotApiRoutes.tokenAuthPath(""), authRequest)
        response <- app.runZIO(request)
        auth <- ZIOHttpClient.getResponse[AuthResponse](response)

        _ <- ref.set(TestProjectState(Some(userId), Some(auth.token), None))
      } yield assertTrue(
        auth.token.nonEmpty,
        auth.role == Role.PreProject
      )
    },

    test("create project") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestProjectState]]
        state <- ref.get
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        app = ZioHttpInterpreter().toHttp(List(ProjectEndpoint.postProjectEndpoint))
        projectRequest = getProjectCreateRequest
        request = ZIOHttpClient.postAuthRequest(TarotApiRoutes.projectCreatePath(""), projectRequest, token)
        response <- app.runZIO(request)
        projectId <- ZIOHttpClient.getResponse[IdResponse](response).map(_.id)

        ref <- ZIO.service[Ref.Synchronized[TestProjectState]]
        _ <- ref.set(TestProjectState(state.userId, Some(token), Some(projectId)))
      } yield assertTrue(projectId.toString.nonEmpty)
    },

    test("auth user admin role") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestProjectState]]
        state <- ref.get
        userId <- ZIO.fromOption(state.userId).orElseFail(TarotError.NotFound("userId not set"))
        projectId <- ZIO.fromOption(state.projectId).orElseFail(TarotError.NotFound("projectId not set"))

        app = ZioHttpInterpreter().toHttp(List(AuthEndpoint.postAuthEndpoint))
        authRequest = getAuthRequest(userId, clientSecret, Some(projectId))
        request = ZIOHttpClient.postRequest(TarotApiRoutes.tokenAuthPath(""), authRequest)
        response <- app.runZIO(request)
        auth <- ZIOHttpClient.getResponse[AuthResponse](response)
      } yield assertTrue(
        auth.token.nonEmpty,
        auth.role == Role.Admin
      )
    }
  ).provideShared(
    Scope.default,
    TestTarotEnvLayer.testEnvLive,
    testProjectStateLayer
  ) @@ sequential

  private val testProjectStateLayer: ZLayer[Any, Nothing, Ref.Synchronized[TestProjectState]] =
    ZLayer.fromZIO(Ref.Synchronized.make(TestProjectState(None, None, None)))

  private def getUserCreateRequest(clientId: String, clientSecret: String) =
    UserCreateRequest(
      clientId = clientId,
      clientSecret = clientSecret,
      name = "Test User"
    )

  private def getAuthRequest(userId: UUID, clientSecret: String, projectId: Option[UUID]) =
    AuthRequest(
      clientType = ClientType.Telegram,
      userId = userId,
      clientSecret = clientSecret,
      projectId = projectId
    )

  private def getProjectCreateRequest =
    ProjectCreateRequest(
      name = "Test project"
    )
}
