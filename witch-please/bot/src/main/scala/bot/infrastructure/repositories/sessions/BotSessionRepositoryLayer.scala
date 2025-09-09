package bot.infrastructure.repositories.sessions

import bot.domain.models.session.BotSession
import bot.infrastructure.repositories.*
import zio.{Ref, ULayer, ZLayer}

object BotSessionRepositoryLayer {
  val botSessionRepositoryLive: ULayer[BotSessionRepository] =
    ZLayer.fromZIO(
      Ref.Synchronized.make(Map.empty[Long, BotSession]).map(new BotSessionRepositoryLive(_))
    )
}
