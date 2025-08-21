package bot.infrastructure.services.sessions

import bot.domain.models.session.{BotPendingAction, BotSession}
import bot.infrastructure.services.authorize.SecretService
import bot.layers.AppEnv
import shared.infrastructure.services.common.DateTimeService
import zio.ZIO

import java.util.UUID

final class BotSessionServiceLive extends BotSessionService {
  def get(chatId: Long): ZIO[AppEnv, Throwable, BotSession] =
    for {
      _ <- ZIO.logDebug(s"Getting session for chat $chatId")

      botSessionRepository <- ZIO.serviceWith[AppEnv](_.botRepository.botSessionRepository)
      currentSession <- botSessionRepository.get(chatId)
        .someOrFail(new RuntimeException(s"Session not found for chat $chatId"))
    } yield currentSession

  def start(chatId: Long): ZIO[AppEnv, Throwable, BotSession] =
    for {
      _ <- ZIO.logDebug(s"Starting session for chat $chatId")

      botSessionRepository <- ZIO.serviceWith[AppEnv](_.botRepository.botSessionRepository)
      currentSession <- botSessionRepository.get(chatId)
      now <- DateTimeService.getDateTimeNow
      session <- currentSession match {
        case Some(session) =>
          val updated = BotSession.touched(session, now)
          botSessionRepository.put(chatId, updated).as(updated)
        case _ =>
          for {
            clientSecret <- SecretService.generateSecret()
            newSession = BotSession.newSession(clientSecret, now)
            _ <- botSessionRepository.put(chatId, newSession)
          } yield newSession
      }
    } yield session

  def setUser(chatId: Long, userId: UUID, token: String): ZIO[AppEnv, Nothing, Unit] =
    for {
      _ <- ZIO.logDebug(s"Set user $userId for chat $chatId")

      now <- DateTimeService.getDateTimeNow
      botSessionRepository <- ZIO.serviceWith[AppEnv](_.botRepository.botSessionRepository)
      _ <- botSessionRepository.update(chatId)(session => BotSession.withUser(session, userId, token, now))
    } yield ()

  def clearPending(chatId: Long): ZIO[AppEnv, Nothing, Unit] =
    for {
      _ <- ZIO.logDebug(s"Clear pending action for chat $chatId")

      now <- DateTimeService.getDateTimeNow
      botSessionRepository <- ZIO.serviceWith[AppEnv](_.botRepository.botSessionRepository)
      _ <- botSessionRepository.update(chatId)(session => BotSession.withPending(session, None, now))
    } yield ()

  def setPending(chatId: Long, pending: BotPendingAction): ZIO[AppEnv, Nothing, Unit] =
    for {
      _ <- ZIO.logDebug(s"Set pending action $pending for chat $chatId")

      now <- DateTimeService.getDateTimeNow
      botSessionRepository <- ZIO.serviceWith[AppEnv](_.botRepository.botSessionRepository)
      _ <- botSessionRepository.update(chatId)(session => BotSession.withPending(session, Some(pending), now))
    } yield ()

  def setProject(chatId: Long, projectId: UUID, token: String): ZIO[AppEnv, Nothing, Unit] =
    for {
      _ <- ZIO.logDebug(s"Set project $projectId for chat $chatId")

      now <- DateTimeService.getDateTimeNow
      botSessionRepository <- ZIO.serviceWith[AppEnv](_.botRepository.botSessionRepository)
      _ <- botSessionRepository.update(chatId)(session => BotSession.withProject(session, projectId, token, now))
    } yield ()
}
