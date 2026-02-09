package tarot.integration

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.cards.CardResponse
import shared.api.dto.tarot.cardsOfDay.CardOfDayResponse
import shared.api.dto.tarot.common.IdResponse
import shared.api.dto.tarot.photo.PhotoResponse
import shared.api.dto.tarot.spreads.*
import shared.api.dto.tarot.users.AuthorResponse
import shared.infrastructure.services.clients.ZIOHttpClient
import shared.models.tarot.authorize.ClientType
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import tarot.api.endpoints.*
import tarot.domain.models.TarotError
import tarot.domain.models.spreads.SpreadId
import tarot.fixtures.{TarotTestFixtures, TarotTestRequests}
import tarot.integration.SpreadPublishIntegrationSpec.{cardsCount, channelId, clientId, clientSecret, clientType, test}
import tarot.layers.{TarotEnv, TestTarotEnvLayer}
import tarot.models.TestSpreadState
import zio.*
import zio.http.*
import zio.json.*
import zio.test.*
import zio.test.TestAspect.sequential

import java.util.UUID

object PhotoIntegrationSpec extends ZIOSpecDefault {
  private final val cardsCount = 2
  private final val clientId = "123456789"
  private final val channelId = 12345
  private final val clientType = ClientType.Telegram
  private final val clientSecret = "test-secret-token"

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Spread API integration")(
    test("initialize test state") {
      for {
        photoSourceId <- TarotTestFixtures.createPhoto(channelId)
        userId <- TarotTestFixtures.createUser(clientId, clientType, clientSecret)
        _ <- TarotTestFixtures.createUserChannel(userId, channelId)
        spreadId <- TarotTestFixtures.createSpread(userId, cardsCount, photoSourceId)
        token <- TarotTestFixtures.createToken(clientType, clientSecret, userId)

        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state = TestSpreadState.empty.withPhotoSourceId(photoSourceId).withUserId(userId.id).withToken(token).withSpreadId(spreadId.id)
        _ <- ref.set(state)
      } yield assertTrue(photoSourceId.nonEmpty, token.nonEmpty)
    },

    test("should get photos") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        photoSourceId <- ZIO.fromOption(state.photoSourceId).orElseFail(TarotError.NotFound("photoId not set"))

        spreadHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
        spread <- spreadHandler.getSpread(SpreadId(spreadId))

        app = ZioHttpInterpreter().toHttp(PhotoEndpoint.endpoints)
        request = ZIOHttpClient.getAuthRequest(TarotApiRoutes.photoGetPath("", spread.photo.id.id), token)
        response <- app.runZIO(request)
        photo <- ZIOHttpClient.getResponse[PhotoResponse](response)
      } yield assertTrue(
        photo.sourceId == photoSourceId)
    },
  ).provideShared(
    Scope.default,
    TestTarotEnvLayer.testEnvLive,
    testSpreadStateLayer
  ) @@ sequential

  private val testSpreadStateLayer: ZLayer[Any, Nothing, Ref.Synchronized[TestSpreadState]] =
    ZLayer.fromZIO(Ref.Synchronized.make(TestSpreadState.empty))
}
