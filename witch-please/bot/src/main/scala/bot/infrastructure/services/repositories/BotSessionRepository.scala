package bot.infrastructure.services.repositories

import bot.domain.models.session.*
import zio.ZIO

trait BotSessionRepository {
  def get(clientId: Long): ZIO[Any, Throwable, Option[BotSession]]
  def upsert(clientId: Long, session: BotSession): ZIO[Any, Throwable, Unit]
  def modify(clientId: Long, session: BotSession): ZIO[Any, Throwable, BotSession]
}
