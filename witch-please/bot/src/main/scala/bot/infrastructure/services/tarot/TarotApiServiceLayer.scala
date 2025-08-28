package bot.infrastructure.services.tarot

import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.{Task, ZLayer}

object TarotApiServiceLayer {
  val tarotApiServiceLive: ZLayer[String, Throwable, TarotApiService] =
    AsyncHttpClientZioBackend.layer() ++ ZLayer.service[String] >>>
      ZLayer.fromFunction { (baseUrl: String, client: SttpBackend[Task, Any]) =>
        TarotApiServiceLive(baseUrl, client)
      }
}
