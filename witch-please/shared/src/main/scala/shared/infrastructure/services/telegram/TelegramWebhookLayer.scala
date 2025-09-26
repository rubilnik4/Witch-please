package shared.infrastructure.services.telegram

import shared.application.configurations.TelegramConfig
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.{Task, ZLayer}

object TelegramWebhookLayer {
  val telegramWebhookLive: ZLayer[TelegramConfig, Throwable, TelegramWebhookService] =
    AsyncHttpClientZioBackend.layer() ++ ZLayer.service[TelegramConfig] >>>
      ZLayer.fromFunction { (config: TelegramConfig, client: SttpBackend[Task, Any]) =>
        TelegramWebhookServiceLive(config, client)
      }
}
