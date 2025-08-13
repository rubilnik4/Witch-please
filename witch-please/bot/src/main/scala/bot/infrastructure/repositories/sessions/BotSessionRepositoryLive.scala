package bot.infrastructure.repositories.sessions

import bot.domain.models.session.*
import shared.infrastructure.services.common.DateTimeService
import zio.{Duration, Ref, UIO, ZIO}

import java.time.Instant

final class BotSessionRepositoryLive(ref: Ref[Map[Long, BotSession]]) extends BotSessionRepository {
  def get(chatId: Long): UIO[Option[BotSession]] =
    ref.get.map(_.get(chatId))

  def upsert(session: BotSession, now: Instant): UIO[Unit] =
    ref.update(_.updated(session.chatId, session.copy(updatedAt = now))).unit

  def update(chatId: Long)(updateSession: BotSession => BotSession, now: Instant): UIO[Unit] =
    ref.update { sessions =>
      sessions.get(chatId) match {
        case Some(session) =>
          val newSession = updateSession(session).copy(updatedAt = now)
          sessions.updated(chatId, newSession)
        case None => sessions
      }
    }.unit

  def delete(chatId: Long): UIO[Unit] =
    ref.update(_.removed(chatId)).unit
}
