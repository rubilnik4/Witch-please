package tarot.integration

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.spreads.*
import shared.infrastructure.services.clients.ZIOHttpClient
import shared.models.tarot.authorize.ClientType
import shared.models.tarot.spreads.SpreadStatus
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import tarot.api.endpoints.*
import tarot.domain.models.TarotError
import tarot.domain.models.cards.CardId
import tarot.domain.models.spreads.*
import tarot.fixtures.{TarotTestFixtures, TarotTestRequests}
import tarot.integration.SpreadIntegrationSpec.test
import tarot.layers.{TarotEnv, TestTarotEnvLayer}
import tarot.models.TestSpreadState
import zio.*
import zio.http.*
import zio.json.*
import zio.test.*
import zio.test.TestAspect.sequential

object SpreadModifyIntegrationSpec extends ZIOSpecDefault {
  private final val cardsCount = 3
  private final val clientId = "123456789"
  private final val clientType = ClientType.Telegram
  private final val clientSecret = "test-secret-token"

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Spread modify API integration")(
    test("initialize test state") {
      for {
        photoId <- TarotTestFixtures.createPhoto
        userId <- TarotTestFixtures.createUser(clientId, clientType, clientSecret)
        spreadId <- TarotTestFixtures.createSpread(userId, cardsCount, photoId)
        cardIds <- TarotTestFixtures.createCards(spreadId, cardsCount, photoId)
        token <- TarotTestFixtures.createToken(clientType, clientSecret, userId)

        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        _ <- ref.set(TestSpreadState(Some(photoId), Some(userId.id), Some(token), Some(spreadId.id), Some(cardIds.map(_.id))))
      } yield assertTrue(photoId.nonEmpty, token.nonEmpty)
    },

    test("should update spread") {
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

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        spreadRequest = TarotTestRequests.spreadUpdateRequest(cardsCount - 1, photoId)
        request = ZIOHttpClient.putAuthRequest(TarotApiRoutes.spreadUpdatePath("", spreadId), spreadRequest, token)
        _ <- app.runZIO(request)

        spread <- spreadQueryHandler.getSpread(SpreadId(spreadId))
        spreadCardsCount <- cardQueryHandler.getCardsCount(SpreadId(spreadId))
        spreadPhotoExist <- photoQueryHandler.existPhoto(previousSpread.photo.id)
      } yield assertTrue(
        spread.id.id == spreadId,
        spreadCardsCount == previousSpread.cardsCount,
        spread.photo.sourceId == photoId,
        !spreadPhotoExist
      )
    },

    test("should update card") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(TarotError.NotFound("photoId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        cardId <- ZIO.fromOption(state.cardIds.flatMap(_.headOption)).orElseFail(TarotError.NotFound("cardIds not set"))
        
        cardQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardQueryHandler)
        photoQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.photoQueryHandler)
        previousCard <- cardQueryHandler.getCard(CardId(cardId))        

        app = ZioHttpInterpreter().toHttp(CardEndpoint.endpoints)
        cardRequest = TarotTestRequests.cardUpdateRequest(photoId)
        request = ZIOHttpClient.putAuthRequest(TarotApiRoutes.cardUpdatePath("", cardId), cardRequest, token)
        _ <- app.runZIO(request)

        card <- cardQueryHandler.getCard(CardId(cardId))
        cardPhotoExist <- photoQueryHandler.existPhoto(previousCard.photo.id)
      } yield assertTrue(
        card.id.id == cardId,
        card.photo.sourceId == photoId,
        !cardPhotoExist
      )
    },

    test("should schedule spread") {
      for {
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        publishRequest <- TarotTestRequests.spreadPublishRequest
        request = ZIOHttpClient.putAuthRequest(TarotApiRoutes.spreadPublishPath("", spreadId), publishRequest, token)
        _ <- app.runZIO(request)

        spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
        cardQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardQueryHandler)
        spread <- spreadQueryHandler.getSpread(SpreadId(spreadId))
        cardsCount <- cardQueryHandler.getCardsCount(SpreadId(spreadId))
      } yield assertTrue(
        spread.status == SpreadStatus.Scheduled,
        spread.scheduledAt.contains(publishRequest.scheduledAt),
        cardsCount == spread.cardsCount
      )
    }

  ).provideShared(
    Scope.default,
    TestTarotEnvLayer.testEnvLive,
    testSpreadStateLayer
  ) @@ sequential

  private val testSpreadStateLayer: ZLayer[Any, Nothing, Ref.Synchronized[TestSpreadState]] =
    ZLayer.fromZIO(Ref.Synchronized.make(TestSpreadState(None, None, None, None, None)))  
}
