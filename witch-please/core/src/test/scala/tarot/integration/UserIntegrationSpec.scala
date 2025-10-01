package tarot.integration

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.common.IdResponse
import shared.api.dto.tarot.users.*
import shared.infrastructure.services.clients.ZIOHttpClient
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.ztapir.*
import tarot.api.endpoints.*
import tarot.data.UserData
import tarot.domain.models.TarotError
import tarot.layers.{TarotEnv, TestTarotEnvLayer}
import tarot.models.TestProjectState
import zio.*
import zio.http.*
import zio.json.*
import zio.test.*
import zio.test.TestAspect.sequential

import java.util.UUID

object UserIntegrationSpec extends ZIOSpecDefault {
  private val clientId = UserData.generateClientId()
  private val clientSecret = UserData.generateClientSecret()

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("User API integration")(
    test("create user") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestProjectState]]

        app = ZioHttpInterpreter().toHttp(UserEndpoint.endpoints)        
        userRequest = getUserCreateRequest(clientId, clientSecret)
        request = ZIOHttpClient.postRequest(TarotApiRoutes.userCreatePath(""), userRequest)
        response <- app.runZIO(request)
        userId <- ZIOHttpClient.getResponse[IdResponse](response).map(_.id)

        _ <- ref.set(TestProjectState(Some(userId), None, None))
      } yield assertTrue(userId.toString.nonEmpty)
    },

    test("get user") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestProjectState]]
        state <- ref.get
        userId <- ZIO.fromOption(state.userId).orElseFail(TarotError.NotFound("userId not set"))
        
        app = ZioHttpInterpreter().toHttp(UserEndpoint.endpoints)
        request = ZIOHttpClient.getRequest(TarotApiRoutes.userGetByClientIdPath("", clientId))
        response <- app.runZIO(request)        
        user <- ZIOHttpClient.getResponse[UserResponse](response)
      } yield assertTrue(
        user.id == userId,
        user.clientId == clientId
      )
    },

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
}
