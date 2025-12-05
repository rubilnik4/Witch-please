package tarot.integration

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.cards.CardCreateRequest
import shared.api.dto.tarot.common.IdResponse
import shared.api.dto.tarot.photo.PhotoRequest
import shared.api.dto.tarot.spreads.*
import shared.infrastructure.services.clients.ZIOHttpClient
import shared.infrastructure.services.common.DateTimeService
import shared.models.files.FileSourceType
import shared.models.tarot.authorize.ClientType
import shared.models.tarot.spreads.SpreadStatus
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import tarot.api.endpoints.*
import tarot.application.jobs.spreads.SpreadPublishType
import tarot.domain.models.TarotError
import tarot.domain.models.spreads.SpreadId
import tarot.fixtures.{TarotTestFixtures, TarotTestRequests}
import tarot.integration.SpreadPublishIntegrationSpec.test
import tarot.layers.{TarotEnv, TestTarotEnvLayer}
import tarot.models.TestSpreadState
import zio.*
import zio.http.*
import zio.json.*
import zio.test.*
import zio.test.TestAspect.sequential

import java.time.Instant
import java.util.UUID

object SpreadPublishIntegrationSpec extends ZIOSpecDefault {
  private final val cardsCount = 1
  private final val clientId = "123456789"
  private final val clientType = ClientType.Telegram
  private final val clientSecret = "test-secret-token"

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Spread publish API integration")(
    test("initialize test state") {
      for {
        photoId <- TarotTestFixtures.createPhoto
        userId <- TarotTestFixtures.createUser(clientId, clientType, clientSecret)
        spreadId <- TarotTestFixtures.createSpread(userId, cardsCount, photoId)
        token <- TarotTestFixtures.createToken(clientType, clientSecret, userId)
        
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        _ <- ref.set(TestSpreadState(Some(photoId), Some(userId.id), Some(token), Some(spreadId.id)))
      } yield assertTrue(photoId.nonEmpty, token.nonEmpty)
    },

    test("can't publish spread without cards") {
      for {
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        config <- ZIO.serviceWith[TarotEnv](_.config.publish)
        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        publishTime <- DateTimeService.getDateTimeNow.map(time => time.plus(10.minute))
        publishRequest = TarotTestRequests.spreadPublishRequest(publishTime, config.maxCardOfDayDelay)
        request = ZIOHttpClient.putAuthRequest(TarotApiRoutes.spreadPublishPath("", spreadId), publishRequest, token)
        response <- app.runZIO(request)

      } yield assertTrue(response.status == Status.Conflict)
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
        }
      } yield assertTrue(cardIds.length == cardsCount)
    },

    test("can't publish spread when scheduledAt is after maxFutureTime") {
      for {
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        config <- ZIO.serviceWith[TarotEnv](_.config.publish)
        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        publishTime <- DateTimeService.getDateTimeNow.map(time => time.plus(config.maxFutureTime).plus(1.minute))
        publishRequest = TarotTestRequests.spreadPublishRequest(publishTime, config.maxCardOfDayDelay)
        request = ZIOHttpClient.putAuthRequest(TarotApiRoutes.spreadPublishPath("", spreadId), publishRequest, token)
        response <- app.runZIO(request)

      } yield assertTrue(
        response.status == Status.BadRequest
      )
    },

    test("can't publish spread when scheduledAt is before hardPastTime") {
      for {
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        config <- ZIO.serviceWith[TarotEnv](_.config.publish)
        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        publishTime <- DateTimeService.getDateTimeNow.map(time => time.minus(config.hardPastTime))
        publishRequest = TarotTestRequests.spreadPublishRequest(publishTime, config.maxCardOfDayDelay)
        request = ZIOHttpClient.putAuthRequest(TarotApiRoutes.spreadPublishPath("", spreadId), publishRequest, token)
        response <- app.runZIO(request)

      } yield assertTrue(
        response.status == Status.BadRequest
      )
    },

    test("can't publish spread when card of day delay more than max") {
      for {
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        maxCardOfDayDelay <- ZIO.serviceWith[TarotEnv](_.config.publish.maxCardOfDayDelay)
        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        publishTime <- DateTimeService.getDateTimeNow.map(time => time.plus(10.minute))
        publishRequest = TarotTestRequests.spreadPublishRequest(publishTime, maxCardOfDayDelay.plus(1.hours))
        request = ZIOHttpClient.putAuthRequest(TarotApiRoutes.spreadPublishPath("", spreadId), publishRequest, token)
        response <- app.runZIO(request)

      } yield assertTrue(
        response.status == Status.BadRequest
      )
    },

    test("should schedule after deadline spread") {
      for {
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        config <- ZIO.serviceWith[TarotEnv](_.config.publish)
        deadline = config.lookAhead.plus(5.second)
        publishTime <- DateTimeService.getDateTimeNow.map(_.plus(deadline))

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        publishRequest = TarotTestRequests.spreadPublishRequest(publishTime, config.maxCardOfDayDelay)
        request = ZIOHttpClient.putAuthRequest(TarotApiRoutes.spreadPublishPath("", spreadId), publishRequest, token)
        _ <- app.runZIO(request)

        spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
        spread <- spreadQueryHandler.getSpread(SpreadId(spreadId))
      } yield assertTrue(
        spread.spreadStatus == SpreadStatus.Scheduled,
        spread.scheduledAt.contains(publishRequest.scheduledAt)
      )
    },

    test("shouldn't take spreads after deadline") {
      for {
        spreadJob <- ZIO.serviceWith[TarotEnv](_.jobs.spreadJob)

        spreadIds <- spreadJob.publishSpreads().map(_.map(_.id))
      } yield assertTrue(
        spreadIds.isEmpty
      )
    },

    test("should reschedule lookahead spread") {
      for {
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        publishTime <- DateTimeService.getDateTimeNow
        config <- ZIO.serviceWith[TarotEnv](_.config.publish)
        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        publishRequest = TarotTestRequests.spreadPublishRequest(publishTime, config.maxCardOfDayDelay)
        request = ZIOHttpClient.putAuthRequest(TarotApiRoutes.spreadPublishPath("", spreadId), publishRequest, token)
        _ <- app.runZIO(request)

        spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
        spread <- spreadQueryHandler.getSpread(SpreadId(spreadId))
      } yield assertTrue(
        spread.spreadStatus == SpreadStatus.Scheduled,
        spread.scheduledAt.contains(publishRequest.scheduledAt)
      )
    },

    test("should take lookahead preview spreads") {
      for {
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))

        spreadJob <- ZIO.serviceWith[TarotEnv](_.jobs.spreadJob)

        result <- spreadJob.publishSpreads()
        previewResult = result.filter(_.publishType == SpreadPublishType.PreviewPublished).map(_.id)
        publishResult = result.filter(_.publishType == SpreadPublishType.Published).map(_.id)
      } yield assertTrue(
        previewResult.map(_.id).contains(spreadId),
        publishResult.isEmpty
      )
    },

    test("can't delete preview published spread") {
      for {
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        request = ZIOHttpClient.deleteAuthRequest(TarotApiRoutes.spreadDeletePath("", spreadId), token)
        response <- app.runZIO(request)
      } yield assertTrue(
        response.status == Status.Conflict
      )
    },

    test("can't create cards on preview published spread") {
      for {
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
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

    test("should take lookahead spreads") {
      for {
        config <- ZIO.serviceWith[TarotEnv](_.config.publish)
        _ <- TestClock.adjust(config.maxCardOfDayDelay)
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))

        spreadJob <- ZIO.serviceWith[TarotEnv](_.jobs.spreadJob)

        result <- spreadJob.publishSpreads()
        previewResult = result.filter(_.publishType == SpreadPublishType.PreviewPublished).map(_.id)
        publishedResult = result.filter(_.publishType == SpreadPublishType.Published).map(_.id)
      } yield assertTrue(
        previewResult.isEmpty,
        publishedResult.map(_.id).contains(spreadId)
      )
    },

    test("can't delete published card") {
      for {
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))

        cardQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardQueryHandler)
        card <- cardQueryHandler.getCards(SpreadId(spreadId))
          .flatMap(cards => ZIO.fromOption(cards.headOption).orElseFail(TarotError.NotFound("card not set")))
        cardId = card.id.id

        app = ZioHttpInterpreter().toHttp(CardEndpoint.endpoints)
        request = ZIOHttpClient.deleteAuthRequest(TarotApiRoutes.cardDeletePath("", cardId), token)
        response <- app.runZIO(request)
      } yield assertTrue(
        response.status == Status.Conflict
      )
    },

    test("can't delete published spread") {
      for {
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        request = ZIOHttpClient.deleteAuthRequest(TarotApiRoutes.spreadDeletePath("", spreadId), token)
        response <- app.runZIO(request)
      } yield assertTrue(
        response.status == Status.Conflict
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
