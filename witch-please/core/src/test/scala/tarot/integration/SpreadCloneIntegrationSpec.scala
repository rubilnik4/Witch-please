package tarot.integration

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.common.IdResponse
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
import zio.json.*
import zio.test.*
import zio.test.TestAspect.sequential

object SpreadCloneIntegrationSpec extends ZIOSpecDefault {
  private final val cardsCount = 3
  private final val clientId = "123456789"
  private final val channelId = 12345
  private final val clientType = ClientType.Telegram
  private final val clientSecret = "test-secret-token"

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Spread modify API integration")(
    test("initialize test state") {
      for {
        photoSourceId <- TarotTestFixtures.createPhoto(channelId)
        userId <- TarotTestFixtures.createUser(clientId, clientType, clientSecret)
        _ <- TarotTestFixtures.createUserChannel(userId, channelId)
        spreadId <- TarotTestFixtures.createSpread(userId, cardsCount, photoSourceId)
        cardIds <- TarotTestFixtures.createCards(spreadId, cardsCount, photoSourceId)
        cardOfDayId <- TarotTestFixtures.createCardOfDay(cardIds.head, spreadId, photoSourceId)
        token <- TarotTestFixtures.createToken(clientType, clientSecret, userId)

        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]

        state = TestSpreadState.empty.withPhotoSourceId(photoSourceId).withUserId(userId.id).withToken(token)
          .withSpreadId(spreadId.id).withCardIds(cardIds.map(_.id)).withCardOfDayId(cardOfDayId.id)
        _ <- ref.set(state)
      } yield assertTrue(photoSourceId.nonEmpty, token.nonEmpty)
    },

    test("should clone spread") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        photoSourceId <- ZIO.fromOption(state.photoSourceId).orElseFail(TarotError.NotFound("photoSourceId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        originalSpreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))

        spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
        cardQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardQueryHandler)
        cardOfDayQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardOfDayQueryHandler)
        photoQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.photoQueryHandler)

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        request = ZIOHttpClient.postAuthRequest(TarotApiRoutes.spreadClonePath("", originalSpreadId), token)
        response <- app.runZIO(request)
        spreadId <- ZIOHttpClient.getResponse[IdResponse](response).map(_.id)

        originalSpread <- spreadQueryHandler.getSpread(SpreadId(originalSpreadId))
        originalCards <- cardQueryHandler.getCards(SpreadId(originalSpreadId))
        originalCardsByPosition = originalCards.map(card => card.position -> card).toMap
        originalCardOfDay <- cardOfDayQueryHandler.getCardOfDayBySpread(SpreadId(originalSpreadId))
        spread <- spreadQueryHandler.getSpread(SpreadId(spreadId))
        spreadCardsCount <- cardQueryHandler.getCardsCount(SpreadId(spreadId))
        cards <- cardQueryHandler.getCards(SpreadId(spreadId))
        cardsByPosition = cards.map(card => card.position -> card).toMap
        cardOfDay <- cardOfDayQueryHandler.getCardOfDayBySpread(SpreadId(spreadId))
      } yield assertTrue(
        spread.id.id != originalSpread.id.id ,
        spread.photo.sourceId == originalSpread.photo.sourceId,
        spreadCardsCount == originalSpread.cardsCount,
        originalCardsByPosition.keySet == cardsByPosition.keySet,
        originalCardsByPosition.forall { case (position, originalCard) =>
          val card = cardsByPosition(position)
          originalCard.id.id != card.id.id && originalCard.photo.sourceId == card.photo.sourceId
        },
        cardOfDay.id.id != originalCardOfDay.id.id ,
        cardOfDay.photo.sourceId == originalCardOfDay.photo.sourceId
      )
    },
  ).provideShared(
    Scope.default,
    TestTarotEnvLayer.testEnvLive,
    testSpreadStateLayer
  ) @@ sequential

  private val testSpreadStateLayer: ZLayer[Any, Nothing, Ref.Synchronized[TestSpreadState]] =
    ZLayer.fromZIO(Ref.Synchronized.make(TestSpreadState.empty))  
}
