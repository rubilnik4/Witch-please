package tarot.integration

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.common.IdResponse
import shared.infrastructure.services.clients.ZIOHttpClient
import shared.models.tarot.authorize.ClientType
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import tarot.api.endpoints.*
import tarot.domain.models.TarotError
import tarot.domain.models.cards.Card
import tarot.domain.models.cardsOfDay.CardOfDayId
import tarot.domain.models.spreads.*
import tarot.fixtures.{TarotTestFixtures, TarotTestRequests}
import tarot.integration.SpreadIntegrationSpec.test
import tarot.layers.{TarotEnv, TestTarotEnvLayer}
import tarot.states.TestSpreadState
import zio.*
import zio.http.*
import zio.test.*
import zio.test.TestAspect.sequential

object SpreadDeleteIntegrationSpec extends ZIOSpecDefault {
  private final val cardsCount = 3
  private final val clientId = "123456789"
  private final val channelId = 12345
  private final val clientType = ClientType.Telegram
  private final val clientSecret = "test-secret-token"
  private final val cardOfDayCardPosition = 0

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Spread delete API integration")(
    test("initialize test state") {
      for {
        photoSourceId <- TarotTestFixtures.createPhoto(channelId)
        userId <- TarotTestFixtures.createUser(clientId, clientType, clientSecret)
        spreadId <- TarotTestFixtures.createSpread(userId, cardsCount, photoSourceId)
        cardPositions <- TarotTestFixtures.createCards(spreadId, cardsCount, photoSourceId)
        cardOfDayCardId <- TarotTestFixtures.getCardId(cardPositions, cardOfDayCardPosition)
        cardOfDayId <- TarotTestFixtures.createCardOfDay(cardOfDayCardId, spreadId, photoSourceId)
        token <- TarotTestFixtures.createToken(clientType, clientSecret, userId)

        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state = TestSpreadState.empty.withPhotoSourceId(photoSourceId).withUserId(userId.id).withToken(token)
          .withSpreadId(spreadId.id).withCardPositions(cardPositions).withCardOfDayId(cardOfDayId.id)
        _ <- ref.set(state)
      } yield assertTrue(photoSourceId.nonEmpty, token.nonEmpty)
    },

    test("should delete card") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        photoSourceId <- ZIO.fromOption(state.photoSourceId).orElseFail(TarotError.NotFound("photoSourceId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))

        spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
        cardQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardQueryHandler)
        photoQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.photoQueryHandler)
        cards <- cardQueryHandler.getCards(SpreadId(spreadId))
        card <- ZIO.fromOption(cards.lastOption).orElseFail(TarotError.NotFound("card not set"))

        app = ZioHttpInterpreter().toHttp(CardEndpoint.endpoints)
        deleteRequest = ZIOHttpClient.deleteAuthRequest(TarotApiRoutes.cardDeletePath("", card.id.id), token)
        _ <- app.runZIO(deleteRequest)

        spreadCardsCount <- cardQueryHandler.getCardsCount(SpreadId(spreadId))
        cardPhotoExist <- photoQueryHandler.existPhoto(card.photo.id)

        cards <- cardQueryHandler.getCards(SpreadId(spreadId))
        _ <- ref.set(state.withCardPositions(cards.map(Card.toCardPosition)))
      } yield assertTrue(
        spreadCardsCount == cardsCount - 1,
        !cardPhotoExist
      )
    },

    test("should delete card of day") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        photoSourceId <- ZIO.fromOption(state.photoSourceId).orElseFail(TarotError.NotFound("photoSourceId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        cardOfDayId <- ZIO.fromOption(state.cardOfDayId).orElseFail(TarotError.NotFound("cardOfDayId not set"))

        cardOfDayQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardOfDayQueryHandler)
        photoQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.photoQueryHandler)
        cardOfDay <- cardOfDayQueryHandler.getCardOfDay(CardOfDayId(cardOfDayId))

        app = ZioHttpInterpreter().toHttp(CardOfDayEndpoint.endpoints)
        deleteRequest = ZIOHttpClient.deleteAuthRequest(TarotApiRoutes.cardOfDayUpdatePath("", cardOfDayId), token)
        _ <- app.runZIO(deleteRequest)

        cardOfDayError <- cardOfDayQueryHandler.getCardOfDay(CardOfDayId(cardOfDayId)).flip
        cardOfDayPhotoExist <- photoQueryHandler.existPhoto(cardOfDay.photo.id)
      } yield assertTrue(
        cardOfDayError match {
          case TarotError.NotFound(_) => true
          case _ => false
        },
        !cardOfDayPhotoExist
      )
    },

    test("should create card of day") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        photoSourceId <- ZIO.fromOption(state.photoSourceId).orElseFail(TarotError.NotFound("photoSourceId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        cardPositions <- ZIO.fromOption(state.cardPositions).orElseFail(TarotError.NotFound("cardPositions not set"))
        cardOfDayCardId <- TarotTestFixtures.getCardId(cardPositions, cardOfDayCardPosition)
        
        app = ZioHttpInterpreter().toHttp(CardOfDayEndpoint.endpoints)
        cardOfDayRequest = TarotTestRequests.cardOfDayCreateRequest(cardOfDayCardId.id, photoSourceId)
        request = ZIOHttpClient.postAuthRequest(TarotApiRoutes.cardOfDayCreatePath("", spreadId), cardOfDayRequest, token)
        response <- app.runZIO(request)
        cardOfDayId <- ZIOHttpClient.getResponse[IdResponse](response).map(_.id)

        _ <- ref.set(state.withCardOfDayId(cardOfDayId))
      } yield assertTrue(cardOfDayId.toString.nonEmpty)
    },

    test("should delete card with card of day") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        photoSourceId <- ZIO.fromOption(state.photoSourceId).orElseFail(TarotError.NotFound("photoSourceId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))

        spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
        cardQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardQueryHandler)
        cardOfDayQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardOfDayQueryHandler)
        photoQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.photoQueryHandler)
        previousCardOfDay <- cardOfDayQueryHandler.getCardOfDayBySpread(SpreadId(spreadId))
        cardPositions <- ZIO.fromOption(state.cardPositions).orElseFail(TarotError.NotFound("cardPositions not set"))
        cardOfDayCardId <- TarotTestFixtures.getCardId(cardPositions, cardOfDayCardPosition)
        card <- cardQueryHandler.getCard(cardOfDayCardId)

        app = ZioHttpInterpreter().toHttp(CardEndpoint.endpoints)
        deleteRequest = ZIOHttpClient.deleteAuthRequest(TarotApiRoutes.cardDeletePath("", cardOfDayCardId.id), token)
        _ <- app.runZIO(deleteRequest)

        spreadCardsCount <- cardQueryHandler.getCardsCount(SpreadId(spreadId))
        cardPhotoExist <- photoQueryHandler.existPhoto(card.photo.id)
        cardOfDayError <- cardOfDayQueryHandler.getCardOfDayBySpread(SpreadId(spreadId)).flip
        cardOfDayPhotoExist <- photoQueryHandler.existPhoto(previousCardOfDay.photo.id)

        cards <- cardQueryHandler.getCards(SpreadId(spreadId))
        _ <- ref.set(state.withCardPositions(cards.map(Card.toCardPosition)))
      } yield assertTrue(
        spreadCardsCount == cardsCount - 2,
        !cardPhotoExist,
        cardOfDayError match {
          case TarotError.NotFound(_) => true
          case _ => false
        },
        !cardOfDayPhotoExist
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
