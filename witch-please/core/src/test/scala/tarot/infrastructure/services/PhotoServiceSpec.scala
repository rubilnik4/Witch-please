package tarot.infrastructure.services

import shared.models.files.FileSource
import shared.models.telegram.*
import tarot.domain.models.photo.Photo
import tarot.layers.{TarotEnv, TestTarotEnvLayer}
import zio.*
import zio.nio.file.{Files, Path}
import zio.test.*

object PhotoServiceSpec extends ZIOSpecDefault {
  final val resourcePath = "photos/test.png"
  
  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Telegram Photo Download and Store")(
    test("Photo uploaded to Telegram can be fetched and stored") {
      for {
        fileStorageService <- ZIO.serviceWith[TarotEnv](_.tarotService.fileStorageService)
        photo <- fileStorageService.getResourcePhoto(resourcePath)

        telegramApiService <- ZIO.serviceWith[TarotEnv](_.tarotService.telegramApiService)
        telegramConfig <- ZIO.serviceWith[TarotEnv](_.config.telegram)
        telegramFile = TelegramFile(photo.fileName, photo.bytes)
        photoId <- telegramApiService.sendPhoto(telegramConfig.chatId, telegramFile)

        photoService <- ZIO.serviceWith[TarotEnv](_.tarotService.photoService)
        photoSource <- photoService.fetchAndStore(photoId)

        result <- photoSource match {
          case FileSource.Local(path) =>
            Files.exists(Path(path)).map(assertTrue(_))
          case _ =>
            ZIO.fail("Expected Local photo source")
        }
      } yield result
    }
  ).provideShared(TestTarotEnvLayer.testEnvLive)
}
