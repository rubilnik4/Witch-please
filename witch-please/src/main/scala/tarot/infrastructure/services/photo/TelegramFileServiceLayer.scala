package tarot.infrastructure.services.photo

import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import tarot.application.configurations.AppConfig
import zio.{Task, ZLayer}

object TelegramFileServiceLayer {
  val telegramFileServiceLive: ZLayer[AppConfig, Throwable, TelegramFileService] =
    AsyncHttpClientZioBackend.layer() ++ ZLayer.service[AppConfig] >>>
      ZLayer.fromFunction { (env: AppConfig, client: SttpBackend[Task, Any]) =>
        TelegramFileServiceLive(env.telegram.token, client)
      }
}
