package shared.infrastructure.services.telegram

import shared.application.configurations.TelegramConfig
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.{Task, ZLayer}

object TelegramChannelServiceLayer {
  val telegramChannelServiceLive: ZLayer[TelegramConfig, Throwable, TelegramChannelService] =
    AsyncHttpClientZioBackend.layer() ++ ZLayer.service[TelegramConfig] >>>
      ZLayer.fromFunction { (config: TelegramConfig, client: SttpBackend[Task, Any]) =>
        TelegramChannelServiceLive(config.token, client)
      }
}
