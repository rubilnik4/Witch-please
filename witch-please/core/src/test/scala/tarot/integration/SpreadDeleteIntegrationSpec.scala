package tarot.integration

import shared.api.dto.tarot.TarotApiRoutes
import shared.infrastructure.services.clients.ZIOHttpClient
import shared.models.tarot.authorize.ClientType
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import tarot.api.endpoints.*
import tarot.domain.models.TarotError
import tarot.domain.models.spreads.*
import tarot.fixtures.TarotTestFixtures
import tarot.layers.{TarotEnv, TestTarotEnvLayer}
import tarot.models.TestSpreadState
import zio.*
import zio.http.*
import zio.test.*
import zio.test.TestAspect.sequential

object SpreadDeleteIntegrationSpec extends ZIOSpecDefault {
  private final val cardsCount = 3
  private final val clientId = "123456789"
  private final val clientType = ClientType.Telegram
  private final val clientSecret = "test-secret-token"

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Spread delete API integration")(
    test("initialize test state") {
      for {
        photoId <- TarotTestFixtures.createPhoto
        userId <- TarotTestFixtures.createUser(clientId, clientType, clientSecret)
        spreadId <- TarotTestFixtures.createSpread(userId, cardsCount, photoId)
        _ <- TarotTestFixtures.createCards(spreadId, cardsCount, photoId)
        token <- TarotTestFixtures.createToken(clientType, clientSecret, userId)

        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        _ <- ref.set(TestSpreadState(Some(photoId), Some(userId.id), Some(token), Some(spreadId.id)))
      } yield assertTrue(photoId.nonEmpty, token.nonEmpty)
    },

    test("should delete card") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(TarotError.NotFound("photoId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))

        spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
        cardQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardQueryHandler)
        photoQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.photoQueryHandler)
        previousCards <- cardQueryHandler.getCards(SpreadId(spreadId))
        card <- ZIO.fromOption(previousCards.lastOption).orElseFail(TarotError.NotFound("card not set"))

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        deleteRequest = ZIOHttpClient.deleteAuthRequest(TarotApiRoutes.cardDeletePath("", card.id.id), token)
        _ <- app.runZIO(deleteRequest)

        spreadCardsCount <- cardQueryHandler.getCardsCount(SpreadId(spreadId))
        cardPhotoExist <- photoQueryHandler.existPhoto(card.photo.id)
      } yield assertTrue(
        spreadCardsCount == cardsCount - 1,
        !cardPhotoExist
      )
    },

    test("should delete spread") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(TarotError.NotFound("photoId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))

        spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
        cardQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardQueryHandler)
        photoQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.photoQueryHandler)
        previousSpread <- spreadQueryHandler.getSpread(SpreadId(spreadId))
        previousCards <- cardQueryHandler.getCards(SpreadId(spreadId))

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        deleteRequest = ZIOHttpClient.deleteAuthRequest(TarotApiRoutes.spreadDeletePath("", spreadId), token)
        _ <- app.runZIO(deleteRequest)

        spreadError <- spreadQueryHandler.getSpread(SpreadId(spreadId)).flip
        spreadCardsCount <- cardQueryHandler.getCardsCount(SpreadId(spreadId))
        spreadPhotoExist <- photoQueryHandler.existPhoto(previousSpread.photo.id)
        cardPhotoExist <- photoQueryHandler.existAnyPhoto(previousCards.map(_.photo.id))
      } yield assertTrue(
        spreadError match {
          case TarotError.NotFound(_) => true
          case _ => false
        },
        spreadCardsCount == 0,
        !spreadPhotoExist,
        !cardPhotoExist
      )
    }
  ).provideShared(
    Scope.default,
    TestTarotEnvLayer.testEnvLive,
    testSpreadStateLayer
  ) @@ sequential

  private val testSpreadStateLayer: ZLayer[Any, Nothing, Ref.Synchronized[TestSpreadState]] =
    ZLayer.fromZIO(Ref.Synchronized.make(TestSpreadState(None, None, None, None)))
}
