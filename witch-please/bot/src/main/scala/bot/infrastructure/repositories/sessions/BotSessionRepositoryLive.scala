package bot.infrastructure.repositories.sessions

import bot.domain.models.session.*
import shared.infrastructure.services.common.DateTimeService
import zio.{Duration, Ref, UIO, ZIO}

import java.time.Instant

final class BotSessionRepositoryLive(ref: Ref[Map[Long, BotSession]]) extends BotSessionRepository {
  def get(chatId: Long): UIO[Option[BotSession]] =
    ref.get.map(_.get(chatId))

  def put(chatId: Long, session: BotSession): ZIO[Any, Throwable, Unit] =
    ref.get.flatMap { sessions =>
      if (sessions.contains(chatId))
        ref.update(_.updated(chatId, session))
      else
        ZIO.fail(new NoSuchElementException(s"Session not found: $chatId"))
    }

  def update(chatId: Long)(updateSession: BotSession => BotSession): ZIO[Any, Throwable, Unit] =
    ref.get.flatMap { sessions =>
      sessions.get(chatId) match {
        case Some(session) =>
          ref.update(_.updated(chatId, updateSession(session)))
        case None =>
          ZIO.fail(new NoSuchElementException(s"Session not found: $chatId"))
      }
    }

  def delete(chatId: Long): ZIO[Any, Throwable, Unit] =
    ref.get.flatMap { sessions =>
      if (sessions.contains(chatId))
        ref.update(_.removed(chatId))
      else
        ZIO.fail(new NoSuchElementException(s"Session not found: $chatId"))
    }
}
