package bot.infrastructure.services.sessions

import bot.infrastructure.repositories.sessions.BotSessionRepository
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.{Task, ULayer, ZIO, ZLayer}

object BotSessionServiceLayer {
  val live: ZLayer[BotSessionRepository, Nothing, BotSessionService] =
    ZLayer.fromFunction(BotSessionServiceLive(_))
}
