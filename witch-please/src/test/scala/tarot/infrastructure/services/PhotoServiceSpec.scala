package tarot.infrastructure.services

import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import tarot.infrastructure.services.photo.TarotPhotoService
import zio.*
import zio.test.*
import zio.test.TestAspect.*

object PhotoServiceSpec extends ZIOSpecDefault {
  override def spec = suite("Full Telegram Photo Download and Store")(
    test("Photo uploaded to Telegram can be fetched and stored") {
      for {
        photoService <- ZIO.service[TarotPhotoService]
        fileId <- photoService.fetchAndStore()

        service <- ZIO.service[TarotPhotoService]
        source <- service.fetchAndStore(fileId)

        result <- source match {
          case PhotoSource.Local(path) =>
            ZIO.attempt(JFiles.exists(Paths.get(path))).map(assertTrue)
          case _ =>
            ZIO.fail("Expected Local photo source")
        }
      } yield result
    }
  ).provideShared(appConfigLayer, AsyncHttpClientZioBackend.layer(), photoServiceLayer)
}
