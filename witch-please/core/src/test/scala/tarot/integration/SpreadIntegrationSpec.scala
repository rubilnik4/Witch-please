package tarot.integration

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.cards.CardResponse
import shared.api.dto.tarot.common.IdResponse
import shared.api.dto.tarot.spreads.*
import shared.api.dto.tarot.users.AuthorResponse
import shared.infrastructure.services.clients.ZIOHttpClient
import shared.models.tarot.authorize.ClientType
import shared.models.tarot.spreads.SpreadStatus
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import tarot.api.endpoints.*
import tarot.domain.models.TarotError
import tarot.domain.models.spreads.*
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
  private final val clientType = ClientType.Telegram
  private final val clientSecret = "test-secret-token"

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Spread API integration")(
    test("initialize test state") {
      for {
        photoId <- TarotTestFixtures.createPhoto
        userId <- TarotTestFixtures.createUser(clientId, clientType, clientSecret)
        token <- TarotTestFixtures.createToken(clientType, clientSecret, userId)

        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        _ <- ref.set(TestSpreadState(Some(photoId), Some(userId.id), Some(token), None))
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

        _ <- ref.set(TestSpreadState(Some(photoId), state.userId, Some(token), Some(spreadId)))
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

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        cardIds <- ZIO.foreach(0 until cardsCount) { position =>
          val cardRequest = TarotTestRequests.cardCreateRequest(position, photoId)
          val request = ZIOHttpClient.postAuthRequest(TarotApiRoutes.cardCreatePath("", spreadId), cardRequest, token)
          for {
            response <- app.runZIO(request)
            cardId <- ZIOHttpClient.getResponse[IdResponse](response).map(_.id)
          } yield cardId
        }
      } yield assertTrue(cardIds.forall(id => id.toString.nonEmpty))
    },

    test("can't create existing card to current spread") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(TarotError.NotFound("photoId not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        cardRequest = TarotTestRequests.cardCreateRequest(0, photoId)
        request = ZIOHttpClient.postAuthRequest(TarotApiRoutes.cardCreatePath("", spreadId), cardRequest, token)
        response <- app.runZIO(request)
      } yield assertTrue(
        response.status == Status.Conflict
      )
    },

    test("should get cards") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
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

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        request = ZIOHttpClient.getAuthRequest(TarotApiRoutes.cardsCountGetPath("", spreadId), token)
        response <- app.runZIO(request)
        createdCardsCount <- ZIOHttpClient.getResponse[Int](response)
      } yield assertTrue(
        createdCardsCount == cardsCount
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
