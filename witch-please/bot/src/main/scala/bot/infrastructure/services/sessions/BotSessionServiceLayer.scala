package bot.infrastructure.services.sessions

import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.{Task, ULayer, ZLayer}

object BotSessionServiceLayer {
  val botSessionServiceLive: ULayer[BotSessionService] =
    ZLayer.succeed(new BotSessionServiceLive)
}
