package tarot.integration

import shared.api.dto.tarot.TarotApiRoutes
import shared.infrastructure.services.clients.ZIOHttpClient
import shared.models.tarot.authorize.ClientType
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import tarot.api.endpoints.*
import tarot.domain.models.TarotError
import tarot.domain.models.spreads.*
import tarot.fixtures.TarotTestFixtures
import tarot.integration.SpreadIntegrationSpec.test
import tarot.layers.{TarotEnv, TestTarotEnvLayer}
import tarot.models.TestSpreadState
import zio.*
import zio.http.*
import zio.test.*
import zio.test.TestAspect.sequential

object SpreadDeleteSimpleIntegrationSpec extends ZIOSpecDefault {
  private final val cardsCount = 3
  private final val clientId = "123456789"
  private final val chatId = 12345
  private final val clientType = ClientType.Telegram
  private final val clientSecret = "test-secret-token"

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Spread delete simple API integration")(
    test("initialize test state") {
      for {
        photoId <- TarotTestFixtures.createPhoto(chatId)
        userId <- TarotTestFixtures.createUser(clientId, clientType, clientSecret)
        spreadId <- TarotTestFixtures.createSpread(userId, cardsCount, photoId)       
        token <- TarotTestFixtures.createToken(clientType, clientSecret, userId)

        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]      
        state = TestSpreadState.empty.withPhotoId(photoId).withUserId(userId.id).withToken(token)
          .withSpreadId(spreadId.id)
        _ <- ref.set(state)
      } yield assertTrue(photoId.nonEmpty, token.nonEmpty)
    },
    
    test("should delete spread") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(TarotError.NotFound("photoId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))

        spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)      
        photoQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.photoQueryHandler)
        previousSpread <- spreadQueryHandler.getSpread(SpreadId(spreadId))     

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        deleteRequest = ZIOHttpClient.deleteAuthRequest(TarotApiRoutes.spreadDeletePath("", spreadId), token)
        _ <- app.runZIO(deleteRequest)

        spreadError <- spreadQueryHandler.getSpread(SpreadId(spreadId)).flip     
        spreadPhotoExist <- photoQueryHandler.existPhoto(previousSpread.photo.id)
      } yield assertTrue(
        spreadError match {
          case TarotError.NotFound(_) => true
          case _ => false
        }, 
        !spreadPhotoExist       
      )
    }
  ).provideShared(
    Scope.default,
    TestTarotEnvLayer.testEnvLive,
    testSpreadStateLayer
  ) @@ sequential

  private val testSpreadStateLayer: ZLayer[Any, Nothing, Ref.Synchronized[TestSpreadState]] =
    ZLayer.fromZIO(Ref.Synchronized.make(TestSpreadState.empty))
}
