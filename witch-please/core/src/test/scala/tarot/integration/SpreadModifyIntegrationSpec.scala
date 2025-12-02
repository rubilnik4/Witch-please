package tarot.integration

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.photo.PhotoRequest
import shared.api.dto.tarot.spreads.*
import shared.infrastructure.services.clients.ZIOHttpClient
import shared.infrastructure.services.common.DateTimeService
import shared.models.files.FileSourceType
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
import zio.json.*
import zio.test.*
import zio.test.TestAspect.sequential

object SpreadModifyIntegrationSpec extends ZIOSpecDefault {
  private final val cardCount = 2
  private final val clientId = "123456789"
  private final val clientType = ClientType.Telegram
  private final val clientSecret = "test-secret-token"

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Spread API integration")(
    test("initialize test state") {
      for {
        photoId <- TarotTestFixtures.getPhoto
        userId <- TarotTestFixtures.getUser(clientId, clientType, clientSecret)
        spreadId <- TarotTestFixtures.getSpread(userId, photoId)
        _ <- TarotTestFixtures.getCards(spreadId, cardCount, photoId)
        token <- TarotTestFixtures.getToken(clientType, clientSecret, userId)

        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        _ <- ref.set(TestSpreadState(Some(photoId), Some(userId.id), Some(token), Some(spreadId.id)))
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
        spreadRequest = spreadUpdateRequest(cardCount, photoId)
        request = ZIOHttpClient.putAuthRequest(TarotApiRoutes.spreadUpdatePath("", spreadId), spreadRequest, token)
        _ <- app.runZIO(request)

        spread <- spreadQueryHandler.getSpread(SpreadId(spreadId))
        cardsCount <- cardQueryHandler.getCardsCount(SpreadId(spreadId))
        spreadPhotoExist <- photoQueryHandler.existPhoto(previousSpread.photo.id)
      } yield assertTrue(
        spread.id.id == spreadId,
        cardsCount == spread.cardsCount,
        spread.photo.sourceId == photoId,
        !spreadPhotoExist
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
        cardsCount <- cardQueryHandler.getCardsCount(SpreadId(spreadId))
        spreadPhotoExist <- photoQueryHandler.existPhoto(previousSpread.photo.id)
        cardPhotoExist <- photoQueryHandler.existAnyPhoto(previousCards.map(_.photo.id))
      } yield assertTrue(
        spreadError match {
          case TarotError.NotFound(_) => true
          case _ => false
        },
        cardsCount == 0,
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
    
  private def spreadCreateRequest(cardCount: Int, photoId: String) =
    SpreadCreateRequest(
      title = "Spread integration test",
      cardCount = cardCount,
      photo = PhotoRequest(FileSourceType.Telegram, photoId)
    )

  private def spreadUpdateRequest(cardCount: Int, photoId: String) =
    SpreadUpdateRequest(
      title = "Spread integration test",
      cardCount = cardCount,
      photo = PhotoRequest(FileSourceType.Telegram, photoId)
    )

  private def cardCreateRequest(photoId: String) =
    CardCreateRequest(
      title = "Card integration test",
      photo = PhotoRequest(FileSourceType.Telegram, photoId)
    )

  private def spreadPublishRequest: ZIO[TarotEnv, Nothing, SpreadPublishRequest] =
    for {
      now <- DateTimeService.getDateTimeNow
      publishTime = now.plus(10.minute)
      cardOfDayDelayHours = 2.hours
    } yield SpreadPublishRequest(publishTime, cardOfDayDelayHours)
}
