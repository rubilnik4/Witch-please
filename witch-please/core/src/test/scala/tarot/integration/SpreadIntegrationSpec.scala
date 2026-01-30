package tarot.integration

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.cards.CardResponse
import shared.api.dto.tarot.cardsOfDay.CardOfDayResponse
import shared.api.dto.tarot.common.IdResponse
import shared.api.dto.tarot.spreads.*
import shared.api.dto.tarot.users.AuthorResponse
import shared.infrastructure.services.clients.ZIOHttpClient
import shared.models.tarot.authorize.ClientType
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import tarot.api.endpoints.*
import tarot.domain.models.TarotError
import tarot.fixtures.{TarotTestFixtures, TarotTestRequests}
import tarot.layers.{TarotEnv, TestTarotEnvLayer}
import tarot.models.TestSpreadState
import zio.*
import zio.http.*
import zio.json.*
import zio.test.*
import zio.test.TestAspect.sequential

import java.util.UUID

object SpreadIntegrationSpec extends ZIOSpecDefault {
  private final val cardsCount = 2
  private final val clientId = "123456789"
  private final val channelId = 12345
  private final val clientType = ClientType.Telegram
  private final val clientSecret = "test-secret-token"

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Spread API integration")(
    test("initialize test state") {
      for {
        photoId <- TarotTestFixtures.createPhoto(channelId)
        userId <- TarotTestFixtures.createUser(clientId, clientType, clientSecret)
        token <- TarotTestFixtures.createToken(clientType, clientSecret, userId)

        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state = TestSpreadState.empty.withPhotoId(photoId).withUserId(userId.id).withToken(token)
        _ <- ref.set(state)
      } yield assertTrue(photoId.nonEmpty, token.nonEmpty)
    },

    test("should create spread") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(TarotError.NotFound("photoId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        spreadRequest = TarotTestRequests.spreadCreateRequest(cardsCount, photoId)
        request = ZIOHttpClient.postAuthRequest(TarotApiRoutes.spreadCreatePath(""), spreadRequest, token)
        response <- app.runZIO(request)
        spreadId <- ZIOHttpClient.getResponse[IdResponse](response).map(_.id)

        _ <- ref.set(state.withSpreadId(spreadId))
      } yield assertTrue(spreadId.toString.nonEmpty)
    },

    test("should get spreads") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        request = ZIOHttpClient.getAuthRequest(TarotApiRoutes.spreadsGetPath(""), token)
        response <- app.runZIO(request)
        spreads <- ZIOHttpClient.getResponse[List[SpreadResponse]](response)
      } yield assertTrue(
          spreads.nonEmpty,
          spreads.head.id == spreadId)
    },

    test("should get authors") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        userId <- ZIO.fromOption(state.userId).orElseFail(TarotError.NotFound("userId not set"))

        app = ZioHttpInterpreter().toHttp(AuthorEndpoint.endpoints)
        request = ZIOHttpClient.getRequest(TarotApiRoutes.authorsGetPath(""))
        response <- app.runZIO(request)
        authors <- ZIOHttpClient.getResponse[List[AuthorResponse]](response)
      } yield assertTrue(
        authors.nonEmpty,
        authors.head.id == userId)
    },
    
    test("should get spread") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        request = ZIOHttpClient.getAuthRequest(TarotApiRoutes.spreadGetPath("", spreadId), token)
        response <- app.runZIO(request)
        spread <- ZIOHttpClient.getResponse[SpreadResponse](response)
      } yield assertTrue(
        spread.id == spreadId)
    },

    test("should send card to current spread") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(TarotError.NotFound("photoId not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        app = ZioHttpInterpreter().toHttp(CardEndpoint.endpoints)
        cardIds <- ZIO.foreach(0 until cardsCount) { position =>
          val cardRequest = TarotTestRequests.cardCreateRequest(position, photoId)
          val request = ZIOHttpClient.postAuthRequest(TarotApiRoutes.cardCreatePath("", spreadId), cardRequest, token)
          for {
            response <- app.runZIO(request)
            cardId <- ZIOHttpClient.getResponse[IdResponse](response).map(_.id)
          } yield cardId
        }.map(_.toList)

        _ <- ref.set(state.withCardIds(cardIds))
      } yield assertTrue(cardIds.forall(id => id.toString.nonEmpty))
    },

    test("can't create existing card to current spread") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(TarotError.NotFound("photoId not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        app = ZioHttpInterpreter().toHttp(CardEndpoint.endpoints)
        cardRequest = TarotTestRequests.cardCreateRequest(0, photoId)
        request = ZIOHttpClient.postAuthRequest(TarotApiRoutes.cardCreatePath("", spreadId), cardRequest, token)
        response <- app.runZIO(request)
      } yield assertTrue(
        response.status == Status.Conflict
      )
    },

    test("should get card") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        cardId <- ZIO.fromOption(state.cardIds.flatMap(_.headOption)).orElseFail(TarotError.NotFound("cardIds not set"))

        app = ZioHttpInterpreter().toHttp(CardEndpoint.endpoints)
        request = ZIOHttpClient.getAuthRequest(TarotApiRoutes.cardGetPath("", cardId), token)
        response <- app.runZIO(request)
        card <- ZIOHttpClient.getResponse[CardResponse](response)
      } yield assertTrue(
        card.id == cardId)
    },

    test("should get cards") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))

        app = ZioHttpInterpreter().toHttp(CardEndpoint.endpoints)
        request = ZIOHttpClient.getAuthRequest(TarotApiRoutes.cardsGetPath("", spreadId), token)
        response <- app.runZIO(request)
        cards <- ZIOHttpClient.getResponse[List[CardResponse]](response)
      } yield assertTrue(
        cards.nonEmpty,
        cards.length == cardsCount)
    },

    test("should get cards count") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))

        app = ZioHttpInterpreter().toHttp(CardEndpoint.endpoints)
        request = ZIOHttpClient.getAuthRequest(TarotApiRoutes.cardsCountGetPath("", spreadId), token)
        response <- app.runZIO(request)
        createdCardsCount <- ZIOHttpClient.getResponse[Int](response)
      } yield assertTrue(
        createdCardsCount == cardsCount
      )
    },

    test("should create card of day") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(TarotError.NotFound("photoId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        cardId <- ZIO.fromOption(state.cardIds.flatMap(_.headOption)).orElseFail(TarotError.NotFound("cardIds not set"))

        app = ZioHttpInterpreter().toHttp(CardOfDayEndpoint.endpoints)
        cardOfDayRequest = TarotTestRequests.cardOfDayCreateRequest(cardId, photoId)
        request = ZIOHttpClient.postAuthRequest(TarotApiRoutes.cardOfDayCreatePath("", spreadId), cardOfDayRequest, token)
        response <- app.runZIO(request)
        cardOfDayId <- ZIOHttpClient.getResponse[IdResponse](response).map(_.id)

        _ <- ref.set(state.withCardOfDayId(cardOfDayId))
      } yield assertTrue(cardOfDayId.toString.nonEmpty)
    },

    test("should get card of day") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        cardOfDayId <- ZIO.fromOption(state.cardOfDayId).orElseFail(TarotError.NotFound("cardOfDayId not set"))

        app = ZioHttpInterpreter().toHttp(CardOfDayEndpoint.endpoints)
        request = ZIOHttpClient.getAuthRequest(TarotApiRoutes.cardOfDayGetPath("", cardOfDayId), token)
        response <- app.runZIO(request)
        cardOfDay <- ZIOHttpClient.getResponse[CardOfDayResponse](response)
      } yield assertTrue(
        cardOfDay.id == cardOfDayId
      )
    },

    test("should get card of day by spread") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))

        app = ZioHttpInterpreter().toHttp(CardOfDayEndpoint.endpoints)
        request = ZIOHttpClient.getAuthRequest(TarotApiRoutes.cardOfDayBySpreadGetPath("", spreadId), token)
        response <- app.runZIO(request)
        cardOfDay <- ZIOHttpClient.getResponse[Option[CardOfDayResponse]](response)
      } yield assertTrue(
        cardOfDay.nonEmpty,
        cardOfDay.exists(_.spreadId == spreadId)
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
