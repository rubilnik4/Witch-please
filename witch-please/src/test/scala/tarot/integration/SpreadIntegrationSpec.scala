package tarot.integration

import tarot.api.dto.TelegramSpreadRequest
import tarot.api.endpoints.{PathBuilder, SpreadEndpoint}
import tarot.infrastructure.services.PhotoServiceSpec.resourcePath
import tarot.infrastructure.services.clients.ZIOHttpClient
import tarot.layers.AppEnv
import tarot.layers.TestAppEnvLayer.testAppEnvLive
import zio.*
import zio.http.*
import zio.json.*
import zio.test.*

object SpreadIntegrationSpec extends ZIOSpecDefault {
  private def createSpreadRequest(photoId: String) =
    TelegramSpreadRequest(
      title = "Spread integration test",
      cardCount = 3,
      coverPhotoId = photoId
    )

  private def getSpreadUrl(serverUrl: String) =
    PathBuilder.getRoutePath(serverUrl, SpreadEndpoint.spreadPath)

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Spread API integration")(
    test("should send photo, get fileId, and create spread") {
      for {
        fileStorageService <- ZIO.serviceWith[AppEnv](_.tarotService.fileStorageService)
        photo <- fileStorageService.getResourcePhoto(resourcePath)
        
        telegramService <- ZIO.serviceWith[AppEnv](_.tarotService.telegramFileService)
        telegramConfig <- ZIO.serviceWith[AppEnv](_.appConfig.telegram)
        photoId <- telegramService.sendPhoto(telegramConfig.chatId, photo)

        projectConfig <- ZIO.serviceWith[AppEnv](_.appConfig.project)
        spreadUrl = getSpreadUrl(projectConfig.serverUrl)
        spreadRequest = createSpreadRequest(photoId)
        response <- ZIOHttpClient.sendPost[TelegramSpreadRequest, String](spreadUrl, spreadRequest)
        uuid = scala.util.Try(response).toOption
      } yield assertTrue(uuid.nonEmpty)
    }

  ).provideShared(
    Scope.default,
    Client.default,
    testAppEnvLive
  )
}
