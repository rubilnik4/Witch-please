package common.infrastructure.services

import zio.{Task, ZLayer}

object TelegramApiServiceLayer {
  val telegramFileServiceLive: ZLayer[AppConfig, Throwable, TelegramApiService] =
    AsyncHttpClientZioBackend.layer() ++ ZLayer.service[AppConfig] >>>
      ZLayer.fromFunction { (env: AppConfig, client: SttpBackend[Task, Any]) =>
        TelegramApiServiceLive(env.telegram.token, client)
      }
}
