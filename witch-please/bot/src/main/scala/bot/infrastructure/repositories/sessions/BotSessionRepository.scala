package bot.infrastructure.repositories.sessions

import bot.domain.models.session.*
import zio.{UIO, ZIO}

import java.time.Instant

trait BotSessionRepository {
  def get(chatId: Long): UIO[Option[BotSession]]
  def put(chatId: Long, session: BotSession): UIO[Unit]
  def update(chatId: Long)(updateSession: BotSession => BotSession): UIO[Unit]
  def delete(chatId: Long): UIO[Unit]
}