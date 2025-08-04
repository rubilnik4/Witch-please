package tarot.infrastructure.services

import tarot.domain.models.photo.{Photo, PhotoSource}
import tarot.layers.{AppEnv, TestAppEnvLayer}
import zio.*
import zio.nio.file.{Files, Path}
import zio.test.*

object PhotoServiceSpec extends ZIOSpecDefault {
  final val resourcePath = "photos/test.png"
  
  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Telegram Photo Download and Store")(
    test("Photo uploaded to Telegram can be fetched and stored") {
      for {
        fileStorageService <- ZIO.serviceWith[AppEnv](_.tarotService.fileStorageService)
        photo <- fileStorageService.getResourcePhoto(resourcePath)

        telegramService <- ZIO.serviceWith[AppEnv](_.tarotService.telegramFileService)
        telegramConfig <- ZIO.serviceWith[AppEnv](_.appConfig.telegram)
        photoId <- telegramService.sendPhoto(telegramConfig.chatId, photo)

        photoService <- ZIO.serviceWith[AppEnv](_.tarotService.photoService)
        photoSource <- photoService.fetchAndStore(photoId)

        result <- photoSource match {
          case PhotoSource.Local(path) =>
            Files.exists(Path(path)).map(assertTrue(_))
          case _ =>
            ZIO.fail("Expected Local photo source")
        }
      } yield result
    }
  ).provideShared(TestAppEnvLayer.testAppEnvLive)
}
