package bot.infrastructure.services.tarot

import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.{Task, ZLayer}

object TarotApiServiceLayer {
  val live: ZLayer[TarotApiUrl, Throwable, TarotApiService] =
    AsyncHttpClientZioBackend.layer() ++ ZLayer.service[TarotApiUrl] >>>
      ZLayer.fromFunction { (apiUrl: TarotApiUrl, client: SttpBackend[Task, Any]) =>
        TarotApiServiceLive(apiUrl, client)
      }
}
