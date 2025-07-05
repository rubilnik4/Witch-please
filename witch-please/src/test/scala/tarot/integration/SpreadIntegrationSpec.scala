package tarot.integration

import tarot.api.dto.tarot.SpreadPublishRequest
import tarot.api.dto.tarot.telegram.{TelegramCardCreateRequest, TelegramSpreadCreateRequest}
import tarot.api.endpoints.PathBuilder
import tarot.domain.models.TarotError
import tarot.domain.models.spreads.{SpreadId, SpreadStatus}
import tarot.infrastructure.services.PhotoServiceSpec.resourcePath
import tarot.infrastructure.services.clients.ZIOHttpClient
import tarot.layers.TestAppEnvLayer.testAppEnvLive
import tarot.layers.{AppEnv, TestServerLayer}
import tarot.models.TestState
import zio.*
import zio.http.*
import zio.json.*
import zio.test.*
import zio.test.TestAspect.sequential

import java.time.temporal.ChronoUnit
import java.util.UUID

object SpreadIntegrationSpec extends ZIOSpecDefault {
  private val cardCount = 2

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Spread API integration")(
    test("initialize test state") {
      for {
        fileStorageService <- ZIO.serviceWith[AppEnv](_.tarotService.fileStorageService)
        telegramService    <- ZIO.serviceWith[AppEnv](_.tarotService.telegramFileService)
        telegramConfig     <- ZIO.serviceWith[AppEnv](_.appConfig.telegram)

        photo <- fileStorageService.getResourcePhoto(resourcePath)
        photoId <- telegramService.sendPhoto(telegramConfig.chatId, photo)

        ref <- ZIO.service[Ref.Synchronized[TestState]]
        _ <- ref.set(TestState(Some(photoId), None))
      } yield assertTrue(photoId.nonEmpty)
    },

    test("should send photo, get fileId, and create spread") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(TarotError.NotFound("photoId not set"))

        projectConfig <- ZIO.serviceWith[AppEnv](_.appConfig.project)
        spreadUrl = createSpreadUrl(projectConfig.serverUrl)
        spreadRequest = spreadCreateRequest(photoId)
        response <- ZIOHttpClient.sendPost[TelegramSpreadCreateRequest, String](spreadUrl, spreadRequest)

        spreadId <- ZIO
          .attempt(UUID.fromString(response))
          .orElseFail(TarotError.ParsingError("UUID", s"Invalid spread UUID returned: $response"))
        _ <- ref.set(TestState(Some(photoId), Some(spreadId)))
      } yield assertTrue(spreadId.toString.nonEmpty)
    },

    test("should send card to current spread") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(TarotError.NotFound("photoId not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))

        projectConfig <- ZIO.serviceWith[AppEnv](_.appConfig.project)
        responses <- ZIO.foreach(0 until cardCount) { index =>
          val cardUrl = createCardUrl(projectConfig.serverUrl, spreadId, index)
          val cardRequest = cardCreateRequest(photoId)

          for {
            response <- ZIOHttpClient.sendPost[TelegramCardCreateRequest, String](cardUrl, cardRequest)
            cardId <- ZIO
              .attempt(UUID.fromString(response))
              .orElseFail(TarotError.ParsingError("UUID", s"Invalid card UUID returned: $response"))
          } yield cardId
        }
      } yield assertTrue(responses.forall(id => id.toString.nonEmpty))
    },

    test("should publish spread") {
      for {
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestState]](_.get)
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))

        projectConfig <- ZIO.serviceWith[AppEnv](_.appConfig.project)
        spreadUrl = publishSpreadUrl(projectConfig.serverUrl, spreadId)
        publishRequest <- spreadPublishRequest()
        _ <- ZIOHttpClient.sendPut[SpreadPublishRequest](spreadUrl, publishRequest)

        repository <- ZIO.serviceWith[AppEnv](_.tarotRepository)
        spread <- repository.getSpread(SpreadId(spreadId))
      } yield assertTrue(
        spread.isDefined,
        spread.exists(_.spreadStatus == SpreadStatus.Ready)
      )
    }
  ).provideShared(
    TestServer.layer,
    Client.default,
    TestServerLayer.serverConfig,
    Driver.default,
    Scope.default,
    testAppEnvLive,
    TestServerLayer.testServerLayer,
    TestServerLayer.testStateLayer
  ) @@ sequential

  private def spreadCreateRequest(photoId: String) =
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

  private def spreadPublishRequest() =
    Clock.instant.map(now =>
      SpreadPublishRequest(
        scheduledAt = now.plus(20, ChronoUnit.MINUTES)
      )
    )

  private def createSpreadUrl(serverUrl: String) =
    val path = s"/api/telegram/spread"
    PathBuilder.getRoutePath(serverUrl, path)

  private def publishSpreadUrl(serverUrl: String, spreadId: UUID) =
    val path = s"/api/spread/$spreadId/publish"
    PathBuilder.getRoutePath(serverUrl, path)

  private def createCardUrl(serverUrl: String, spreadId: UUID, index: Int): URL = {
    val path = s"/api/telegram/spread/$spreadId/cards/$index"
    PathBuilder.getRoutePath(serverUrl, path)
  }
}
