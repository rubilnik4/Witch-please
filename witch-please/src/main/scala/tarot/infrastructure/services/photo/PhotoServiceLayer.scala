package tarot.infrastructure.services.photo

import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import tarot.application.configurations.AppConfig
import tarot.infrastructure.services.photo.FileStorageServiceLayer.localFileStorageServiceLive
import tarot.infrastructure.services.photo.TelegramFileServiceLayer.telegramFileServiceLive
import zio.{Task, ZIO, ZLayer}

import java.nio.file.Paths

object PhotoServiceLayer {
  val photoServiceLive: ZLayer[AppConfig, Throwable, PhotoService] =
    (telegramFileServiceLive ++ localFileStorageServiceLive) >>> ZLayer.fromFunction(
      (telegram: TelegramFileService, storage: FileStorageService) =>
        new PhotoServiceLive(telegram, storage))
}
