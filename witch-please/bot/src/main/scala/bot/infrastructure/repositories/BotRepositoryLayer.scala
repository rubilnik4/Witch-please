package bot.infrastructure.repositories

import bot.infrastructure.repositories.sessions.*
import shared.infrastructure.services.*
import zio.ULayer

object BotRepositoryLayer {
  val live: ULayer[BotSessionRepository] =
    BotSessionRepositoryLayer.live
}
