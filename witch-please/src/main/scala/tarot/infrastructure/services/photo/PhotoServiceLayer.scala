package tarot.infrastructure.services.photo

import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import tarot.application.configurations.AppConfig
import zio.{Task, ULayer, ZIO, ZLayer}

import java.nio.file.Paths
import javax.sql.DataSource

object PhotoServiceLayer {

  private val telegramDownloaderLayer: ZLayer[AppConfig, Throwable, TelegramDownloader] =
    AsyncHttpClientZioBackend.layer() ++ ZLayer.service[AppConfig] >>>
      ZLayer.fromFunction { (env: AppConfig, client: SttpBackend[Task, Any]) =>
        TelegramDownloaderLive(env.telegram.token, client)
      }

  private val localFileStorageServiceLayer: ZLayer[AppConfig, Throwable, FileStorageService] =
    ZLayer.fromZIO {
      for {
        config <- ZIO.service[AppConfig]

        localStorageConfig <- ZIO.fromOption(config.localStorage)
          .tapError(_ => ZIO.logError("Missing local storage config"))
          .orElseFail(new RuntimeException("Local storage config is missing"))

        path <- ZIO
          .attempt(Paths.get(localStorageConfig.path))
          .mapError(ex => new RuntimeException(s"Invalid path '${localStorageConfig.path}': ${ex.getMessage}", ex))

      } yield new LocalFileStorageServiceLive(path)
    }
    
  val photoServiceLive: ZLayer[AppConfig, Throwable, PhotoService] =
    (telegramDownloaderLayer ++ localFileStorageServiceLayer) >>>
      ZLayer.fromFunction(PhotoServiceLive.apply)  
}
