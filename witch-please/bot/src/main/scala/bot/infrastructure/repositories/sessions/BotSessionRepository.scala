package bot.infrastructure.repositories.sessions

import bot.domain.models.session.*
import zio.{UIO, ZIO}

import java.time.Instant

trait BotSessionRepository {
  def get(chatId: Long): UIO[Option[BotSession]]
  def upsert(session: BotSession, now: Instant): UIO[Unit]
  def update(chatId: Long)(updateSession: BotSession => BotSession, now: Instant): UIO[Unit]
  def delete(chatId: Long): UIO[Unit]
}