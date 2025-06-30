package tarot.integration

import tarot.api.dto.TelegramSpreadRequest
import tarot.api.endpoints.{PathBuilder, SpreadEndpoint}
import tarot.api.routes.RoutesLayer
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

  private val serverConfig: ULayer[Server.Config] = ZLayer.succeed(
    Server.Config.default.port(8080)
  )

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Spread API integration")(
    test("should send photo, get fileId, and create spread") {
      for {
        routes <- RoutesLayer.apiRoutesLive.build.map(_.get)
        _ <- TestServer.addRoutes(routes)

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
    testAppEnvLive,
    TestServer.layer,
    Client.default,
    serverConfig,
    Driver.default,
    Scope.default
  )
}
