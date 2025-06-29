package tarot.integration

import tarot.api.dto.TelegramSpreadRequest
import tarot.infrastructure.services.PhotoServiceSpec.resourcePath
import tarot.layers.AppEnv
import tarot.layers.TestAppEnvLayer.testAppEnvLive
import zio.*
import zio.http.*
import zio.json.*
import zio.test.*

import java.util.UUID

object SpreadIntegrationSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Spread API integration")(
    test("should send photo, get fileId, and create spread") {
      for {
        fileStorageService <- ZIO.serviceWith[AppEnv](_.tarotService.fileStorageService)
        photo <- fileStorageService.getResourcePhoto(resourcePath)
        
        telegramService <- ZIO.serviceWith[AppEnv](_.tarotService.telegramFileService)
        telegramConfig <- ZIO.serviceWith[AppEnv](_.appConfig.telegram)
        photoId <- telegramService.sendPhoto(telegramConfig.chatId, photo)
       
        spreadRequest = TelegramSpreadRequest(
          title = "Spread integration test",
          cardCount = 3,
          coverPhotoId = photoId
        )
        
        client <- ZIO.service[Client]
        baseUrl = URL.decode("http://localhost:8080/spread").toOption.get
        
        request = Request
          .post(baseUrl,Body.fromString(spreadRequest.toJson))
          .setHeaders(Headers(Header.ContentType(MediaType.application.json)))
        
        response <- client.request(request)

        body <- response.body.asString
        
        uuid = scala.util.Try(UUID.fromString(body)).toOption
      } yield assertTrue(response.status.isSuccess && uuid.nonEmpty)
    }

  ).provideShared(
    Scope.default,
    //TestServer.layer,
    //apiRoutesLive,
    testAppEnvLive,
    Client.default
  )
}
