package tarot.integration

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.channels.UserChannelResponse
import shared.api.dto.tarot.common.IdResponse
import shared.infrastructure.services.clients.ZIOHttpClient
import shared.models.tarot.authorize.ClientType
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import tarot.api.endpoints.*
import tarot.domain.models.TarotError
import tarot.fixtures.{TarotTestFixtures, TarotTestRequests}
import tarot.layers.{TarotEnv, TestTarotEnvLayer}
import tarot.models.TestChannelState
import zio.*
import zio.http.*
import zio.json.*
import zio.test.*
import zio.test.TestAspect.sequential

object ChannelIntegrationSpec extends ZIOSpecDefault {
  private final val clientId = "123456789"
  private final val chatId = 12345
  private final val clientType = ClientType.Telegram
  private final val clientSecret = "test-secret-token"

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Channel API integration")(
    test("initialize test state") {
      for {      
        userId <- TarotTestFixtures.createUser(clientId, clientType, clientSecret)
        token <- TarotTestFixtures.createToken(clientType, clientSecret, userId)

        ref <- ZIO.service[Ref.Synchronized[TestChannelState]]
        state = TestChannelState.empty.withUserId(userId.id).withToken(token)
        _ <- ref.set(state)
      } yield assertTrue(token.nonEmpty)
    },

    test("should create channel") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestChannelState]]
        state <- ref.get
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        app = ZioHttpInterpreter().toHttp(ChannelEndpoint.endpoints)
        channelRequest = TarotTestRequests.channelCreateRequest(chatId)
        request = ZIOHttpClient.postAuthRequest(TarotApiRoutes.channelCreatePath(""), channelRequest, token)
        response <- app.runZIO(request)
        userChannelId <- ZIOHttpClient.getResponse[IdResponse](response).map(_.id)
      } yield assertTrue(userChannelId.toString.nonEmpty)
    },

    test("get default channel") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestChannelState]]
        state <- ref.get
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        app = ZioHttpInterpreter().toHttp(ChannelEndpoint.endpoints)
        request = ZIOHttpClient.getAuthRequest(TarotApiRoutes.channelDefaultGetPath(""), token)
        response <- app.runZIO(request)
        userChannel <- ZIOHttpClient.getResponse[UserChannelResponse](response)
      } yield assertTrue(
        userChannel.chatId == chatId
      )
    },
  ).provideShared(
    Scope.default,
    TestTarotEnvLayer.testEnvLive,
    testChannelStateLayer
  ) @@ sequential

  private val testChannelStateLayer: ZLayer[Any, Nothing, Ref.Synchronized[TestChannelState]] =
    ZLayer.fromZIO(Ref.Synchronized.make(TestChannelState.empty))
}
