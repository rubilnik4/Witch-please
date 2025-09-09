package bot.infrastructure.repositories.sessions

import bot.domain.models.session.*
import zio.{UIO, ZIO}

import java.time.Instant

trait BotSessionRepository {
  def get(chatId: Long): UIO[Option[BotSession]]
  def create(chatId: Long, session: BotSession): ZIO[Any, Throwable, Unit]
  def put(chatId: Long, session: BotSession): ZIO[Any, Throwable, Unit]
  def update(chatId: Long)(updateSession: BotSession => BotSession): ZIO[Any, Throwable, Unit]
  def updateZIO(chatId: Long)(f: BotSession => ZIO[Any, Throwable, BotSession]): ZIO[Any, Throwable, Unit]
  def delete(chatId: Long): ZIO[Any, Throwable, Unit]
}