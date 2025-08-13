package shared.infrastructure.services.telegram

import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.{Task, ZLayer}

object TelegramApiServiceLayer {
  val telegramApiServiceLive: ZLayer[String, Throwable, TelegramApiService] =
    AsyncHttpClientZioBackend.layer() ++ ZLayer.service[String] >>>
      ZLayer.fromFunction { (token: String, client: SttpBackend[Task, Any]) =>
        TelegramApiServiceLive(token, client)
      }
}
