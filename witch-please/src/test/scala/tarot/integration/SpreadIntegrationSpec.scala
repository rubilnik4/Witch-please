package tarot.integration

import tarot.api.dto.{TelegramCardRequest, TelegramSpreadRequest}
import tarot.api.endpoints.{PathBuilder, SpreadEndpoint}
import tarot.domain.models.TarotError
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
      } yield assertTrue(photoId.toString.nonEmpty)
    },

    test("should send photo, get fileId, and create spread") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(TarotError.NotFound("photoId not set"))

        projectConfig <- ZIO.serviceWith[AppEnv](_.appConfig.project)
        spreadUrl = getSpreadUrl(projectConfig.serverUrl)
        spreadRequest = createSpreadRequest(photoId)
        response <- ZIOHttpClient.sendPost[TelegramSpreadRequest, String](spreadUrl, spreadRequest)

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
          val cardUrl = getCardUrl(projectConfig.serverUrl, spreadId, index)
          val cardRequest = createCardRequest(photoId)

          for {
            response <- ZIOHttpClient.sendPost[TelegramCardRequest, String](cardUrl, cardRequest)
            cardId <- ZIO
              .attempt(UUID.fromString(response))
              .orElseFail(TarotError.ParsingError("UUID", s"Invalid card UUID returned: $response"))
          } yield cardId
        }
      } yield assertTrue(responses.forall(id => id.toString.nonEmpty))
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

  private def createSpreadRequest(photoId: String) =
    TelegramSpreadRequest(
      title = "Spread integration test",
      cardCount = cardCount,
      coverPhotoId = photoId
    )

  private def createCardRequest(photoId: String) =
    TelegramCardRequest(
      description = "Card integration test",
      coverPhotoId = photoId
    )

  private def getSpreadUrl(serverUrl: String) =
    PathBuilder.getRoutePath(serverUrl, SpreadEndpoint.spreadRoute)

  private def getCardUrl(serverUrl: String, spreadId: UUID, index: Int): URL = {
    val path = s"/api/telegram/spread/$spreadId/cards/$index"
    URL.decode(s"$serverUrl$path").toOption.getOrElse(
      throw new IllegalArgumentException(s"Invalid URL: $serverUrl$path")
    )
  }
}
