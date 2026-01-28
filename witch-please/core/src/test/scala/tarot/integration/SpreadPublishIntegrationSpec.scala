package tarot.integration

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.common.IdResponse
import shared.api.dto.tarot.spreads.*
import shared.infrastructure.services.clients.ZIOHttpClient
import shared.infrastructure.services.common.DateTimeService
import shared.models.tarot.authorize.ClientType
import shared.models.tarot.spreads.SpreadStatus
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import tarot.api.endpoints.*
import tarot.application.jobs.publish.PublishJobResult
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

object SpreadPublishIntegrationSpec extends ZIOSpecDefault {
  private final val cardsCount = 1
  private final val clientId = "123456789"
  private final val chatId = 12345
  private final val clientType = ClientType.Telegram
  private final val clientSecret = "test-secret-token"

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Spread publish API integration")(
    test("initialize test state") {
      for {
        photoId <- TarotTestFixtures.createPhoto(chatId)
        userId <- TarotTestFixtures.createUser(clientId, clientType, clientSecret)
        _ <- TarotTestFixtures.createUserChannel(userId, chatId)
        spreadId <- TarotTestFixtures.createSpread(userId, cardsCount, photoId)
        token <- TarotTestFixtures.createToken(clientType, clientSecret, userId)
        
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state = TestSpreadState.empty.withPhotoId(photoId).withUserId(userId.id).withToken(token).withSpreadId(spreadId.id)
        _ <- ref.set(state)
      } yield assertTrue(photoId.nonEmpty, token.nonEmpty)
    },

    test("can't schedule spread without cards") {
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
        }.map(_.toList)

        _ <- ref.set(state.withCardIds(cardIds))
      } yield assertTrue(cardIds.length == cardsCount)
    },

    test("can't schedule spread without card of day") {
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

      } yield assertTrue(response.status == Status.NotFound)
    },

    test("should send card of day to current spread") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(TarotError.NotFound("photoId not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        cardId <- ZIO.fromOption(state.cardIds.flatMap(_.headOption)).orElseFail(TarotError.NotFound("cardIds not set"))

        app = ZioHttpInterpreter().toHttp(CardOfDayEndpoint.endpoints)
        cardOfDayRequest = TarotTestRequests.cardOfDayCreateRequest(cardId, photoId)
        request = ZIOHttpClient.postAuthRequest(TarotApiRoutes.cardOfDayCreatePath("", spreadId), cardOfDayRequest, token)
        response <- app.runZIO(request)
        cardOfDayId <- ZIOHttpClient.getResponse[IdResponse](response).map(_.id)

        _ <- ref.set(state.withCardOfDayId(cardOfDayId))
      } yield assertTrue(cardOfDayId.toString.nonEmpty)
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
        spread.status == SpreadStatus.Scheduled,
        spread.scheduledAt.contains(publishRequest.scheduledAt)
      )
    },

    test("shouldn't take spreads after deadline") {
      for {
        publishJob <- ZIO.serviceWith[TarotEnv](_.jobs.publishJob)

        results <- publishJob.publish()
      } yield assertTrue(
        results.isEmpty
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
        spread.status == SpreadStatus.Scheduled,
        spread.scheduledAt.contains(publishRequest.scheduledAt)
      )
    },

    test("should take lookahead spreads") {
      for {
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))

        publishJob <- ZIO.serviceWith[TarotEnv](_.jobs.publishJob)

        result <- publishJob.publish()
        spreadResults = result.collect { case spread @ PublishJobResult.Spread(_,_) => spread }
        cardOfDayResults = result.collect { case cardOfDay @ PublishJobResult.CardOfDay(_,_) => cardOfDay }
      } yield assertTrue(
        spreadResults.map(_.id.id).contains(spreadId),
        cardOfDayResults.isEmpty
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
    },

    test("can't create cards on published spread") {
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

    test("can't modify card of day on published spread") {
      for {
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
        photoId <- ZIO.fromOption(state.photoId).orElseFail(TarotError.NotFound("photoId not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        cardOfDayId <- ZIO.fromOption(state.cardOfDayId).orElseFail(TarotError.NotFound("cardOfDayId not set"))
        cardId <- ZIO.fromOption(state.cardIds.flatMap(_.lastOption)).orElseFail(TarotError.NotFound("cardIds not set"))

        app = ZioHttpInterpreter().toHttp(CardOfDayEndpoint.endpoints)
        cardOfDayRequest = TarotTestRequests.cardOfDayUpdateRequest(cardId, photoId)
        request = ZIOHttpClient.putAuthRequest(TarotApiRoutes.cardOfDayUpdatePath("", cardOfDayId), cardOfDayRequest, token)
        response <- app.runZIO(request)
      } yield assertTrue(
        response.status == Status.Conflict
      )
    },

    test("can't delete card of day on published spread") {
      for {
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
        photoId <- ZIO.fromOption(state.photoId).orElseFail(TarotError.NotFound("photoId not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        cardOfDayId <- ZIO.fromOption(state.cardOfDayId).orElseFail(TarotError.NotFound("cardOfDayId not set"))
        cardId <- ZIO.fromOption(state.cardIds.flatMap(_.lastOption)).orElseFail(TarotError.NotFound("cardIds not set"))

        app = ZioHttpInterpreter().toHttp(CardOfDayEndpoint.endpoints)      
        request = ZIOHttpClient.deleteAuthRequest(TarotApiRoutes.cardOfDayDeletePath("", cardOfDayId), token)
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
        cardOfDayId <- ZIO.fromOption(state.cardOfDayId).orElseFail(TarotError.NotFound("spreadId not set"))

        publishJob <- ZIO.serviceWith[TarotEnv](_.jobs.publishJob)

        result <- publishJob.publish()
        spreadResults = result.collect { case spread @ PublishJobResult.Spread(_,_) => spread }
        cardOfDayResults = result.collect { case cardOfDay @ PublishJobResult.CardOfDay(_,_) => cardOfDay }
      } yield assertTrue(
        spreadResults.isEmpty,
        cardOfDayResults.map(_.id.id).contains(cardOfDayId)
      )
    },

    test("can't delete published card") {
      for {
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        cardId <- ZIO.fromOption(state.cardIds.flatMap(_.headOption)).orElseFail(TarotError.NotFound("cardIds not set"))

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
    ZLayer.fromZIO(Ref.Synchronized.make(TestSpreadState.empty))
}
