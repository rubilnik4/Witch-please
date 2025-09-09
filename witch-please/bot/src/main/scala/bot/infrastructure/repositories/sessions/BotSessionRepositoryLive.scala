package bot.infrastructure.repositories.sessions

import bot.domain.models.session.*
import shared.infrastructure.services.common.DateTimeService
import zio.{Duration, Ref, UIO, ZIO}

import java.time.Instant

final class BotSessionRepositoryLive(ref: Ref.Synchronized[Map[Long, BotSession]]) extends BotSessionRepository {
  def get(chatId: Long): UIO[Option[BotSession]] =
    ref.get.map(_.get(chatId))

  def create(chatId: Long, session: BotSession): ZIO[Any, Throwable, Unit] =
    ref.get.flatMap { sessions =>
      if (sessions.contains(chatId))
        ZIO.fail(new IllegalStateException(s"Session already exists: $chatId"))
      else
        ref.update(_.updated(chatId, session))
    }
    
  def put(chatId: Long, session: BotSession): ZIO[Any, Throwable, Unit] =
    ref.get.flatMap { sessions =>
      if (sessions.contains(chatId))
        ref.update(_.updated(chatId, session))
      else
        ZIO.fail(new NoSuchElementException(s"Session not found: $chatId"))
    }

  def update(chatId: Long)(update: BotSession => BotSession): ZIO[Any, Throwable, Unit] =
    ref.modify { sessions =>
      sessions.get(chatId) match {
        case Some(session) => ((), sessions.updated(chatId, update(session)))
        case None => (new NoSuchElementException(s"Session not found: $chatId"), sessions)
      }
    }

  def updateZIO(chatId: Long)(update: BotSession => ZIO[Any, Throwable, BotSession]): ZIO[Any, Throwable, Unit] =
    ref.modifyZIO { sessions =>
      sessions.get(chatId) match {
        case Some(session) => update(session).map(s2 => ((), sessions.updated(chatId, s2)))
        case None => ZIO.fail(new NoSuchElementException(s"Session not found: $chatId"))
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
