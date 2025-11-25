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
import tarot.domain.models.TarotError
import tarot.domain.models.spreads.SpreadId
import tarot.fixtures.TarotTestFixtures
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
  private final val clientId = "123456789"
  private final val clientType = ClientType.Telegram
  private final val clientSecret = "test-secret-token"

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Spread publish API integration")(
    test("initialize test state") {
      for {
        photoId <- TarotTestFixtures.getPhoto
        userId <- TarotTestFixtures.getUser(clientId, clientType, clientSecret)
        spreadId <- TarotTestFixtures.getSpread(userId, photoId)
        token <- TarotTestFixtures.getToken(clientType, clientSecret, userId)
        
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        _ <- ref.set(TestSpreadState(Some(photoId), Some(userId.id), Some(token), Some(spreadId.id)))
      } yield assertTrue(photoId.nonEmpty, token.nonEmpty)
    },

    test("can't publish spread without cards") {
      for {
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        publishTime <- DateTimeService.getDateTimeNow.map(time => time.plus(10.minute))
        publishRequest = spreadPublishRequest(publishTime)
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

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        cardRequest = cardCreateRequest(photoId)
        request = ZIOHttpClient.postAuthRequest(TarotApiRoutes.cardCreatePath("", spreadId, 0), cardRequest, token)
        response <- app.runZIO(request)
        cardId <- ZIOHttpClient.getResponse[IdResponse](response).map(_.id)
      } yield assertTrue(cardId.toString.nonEmpty)
    },

    test("can't publish spread when scheduledAt is after maxFutureTime") {
      for {
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        maxFuture <- ZIO.serviceWith[TarotEnv](_.config.project.maxFutureTime)
        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        publishTime <- DateTimeService.getDateTimeNow.map(time => time.plus(maxFuture).plus(1.minute))
        publishRequest = spreadPublishRequest(publishTime)
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

        hardPast <- ZIO.serviceWith[TarotEnv](_.config.project.hardPastTime)
        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        publishTime <- DateTimeService.getDateTimeNow.map(time => time.minus(hardPast))
        publishRequest = spreadPublishRequest(publishTime)
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
        publishRequest = spreadPublishRequest(publishTime)
        request = ZIOHttpClient.putAuthRequest(TarotApiRoutes.spreadPublishPath("", spreadId), publishRequest, token)
        _ <- app.runZIO(request)

        spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.tarotQueryHandler.spreadQueryHandler)
        spread <- spreadQueryHandler.getSpread(SpreadId(spreadId))
      } yield assertTrue(        
        spread.spreadStatus == SpreadStatus.Scheduled,
        spread.scheduledAt.contains(publishRequest.scheduledAt)
      )
    },

    test("shouldn't take spreads after deadline") {
      for {
        spreadJob <- ZIO.serviceWith[TarotEnv](_.tarotJob.spreadJob)

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

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        publishRequest = spreadPublishRequest(publishTime)
        request = ZIOHttpClient.putAuthRequest(TarotApiRoutes.spreadPublishPath("", spreadId), publishRequest, token)
        _ <- app.runZIO(request)

        spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.tarotQueryHandler.spreadQueryHandler)
        spread <- spreadQueryHandler.getSpread(SpreadId(spreadId))
      } yield assertTrue(
        spread.spreadStatus == SpreadStatus.Scheduled,
        spread.scheduledAt.contains(publishRequest.scheduledAt)
      )
    },

    test("should take lookahead spreads") {
      for {
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))

        spreadJob <- ZIO.serviceWith[TarotEnv](_.tarotJob.spreadJob)

        spreadIds <- spreadJob.publishSpreads().map(_.map(_.id))
      } yield assertTrue(
        spreadIds.map(_.id).contains(spreadId)
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
    
  private def spreadCreateRequest(projectId: UUID, cardCount: Int, photoId: String) =
    TelegramSpreadCreateRequest(
      title = "Spread integration test",
      cardCount = cardCount,
      coverPhotoId = photoId
    )

  private def cardCreateRequest(photoId: String) =
    TelegramCardCreateRequest(
      description = "Card integration test",
      coverPhotoId = photoId
    )

  private def spreadPublishRequest(publishTime: Instant) =
    SpreadPublishRequest(publishTime)
}
