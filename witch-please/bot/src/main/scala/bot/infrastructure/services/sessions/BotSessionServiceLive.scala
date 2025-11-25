package bot.infrastructure.services.sessions

import bot.domain.models.session.*
import bot.infrastructure.repositories.sessions.BotSessionRepository
import bot.infrastructure.services.authorize.SecretService
import bot.layers.BotEnv
import shared.infrastructure.services.common.DateTimeService
import zio.ZIO

import java.time.{LocalDate, LocalTime}
import java.util.UUID

final class BotSessionServiceLive(botSessionRepository: BotSessionRepository) extends BotSessionService {
  def get(chatId: Long): ZIO[BotEnv, Throwable, BotSession] =
    for {
      _ <- ZIO.logDebug(s"Getting session for chat $chatId")

      currentSession <- botSessionRepository.get(chatId)
        .someOrFail(new RuntimeException(s"Session not found for chat $chatId"))
    } yield currentSession

  def start(chatId: Long, username: String): ZIO[BotEnv, Throwable, BotSession] =
    for {
      _ <- ZIO.logDebug(s"Starting session for chat $chatId")
      
      config <- ZIO.serviceWith[BotEnv](_.config.project)
      currentSession <- botSessionRepository.get(chatId)
      now <- DateTimeService.getDateTimeNow
      session <- currentSession match {
        case Some(session) =>
          val updated = BotSession.touched(session, now)
          botSessionRepository.put(chatId, updated).as(updated)
        case _ =>
          for {
            clientSecret <- SecretService.generateSecret(chatId, username, config.userSecretPepper)
            newSession = BotSession.newSession(clientSecret, now)
            _ <- botSessionRepository.create(chatId, newSession)
          } yield newSession
      }
    } yield session

  def reset(chatId: Long): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logDebug(s"Reset session for chat $chatId")

      _ <- botSessionRepository.delete(chatId)
    } yield ()

  def setUser(chatId: Long, userId: UUID, token: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logDebug(s"Set user $userId for chat $chatId")

      now <- DateTimeService.getDateTimeNow
      _ <- botSessionRepository.update(chatId)(session => BotSession.withUser(session, userId, token, now))
    } yield ()

  def clearPending(chatId: Long): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logDebug(s"Clear pending action for chat $chatId")

      now <- DateTimeService.getDateTimeNow
      _ <- botSessionRepository.update(chatId)(session => BotSession.withPending(session, None, now))
    } yield ()

  def setPending(chatId: Long, pending: BotPendingAction): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logDebug(s"Set pending action $pending for chat $chatId")

      now <- DateTimeService.getDateTimeNow
      _ <- botSessionRepository.update(chatId)(session => BotSession.withPending(session, Some(pending), now))
    } yield ()

  def setSpread(chatId: Long, spreadId: UUID, spreadProgress: SpreadProgress): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logDebug(s"Set spread $spreadId for chat $chatId")

      now <- DateTimeService.getDateTimeNow
      _ <- botSessionRepository.update(chatId)(session => BotSession.withSpread(session, spreadId, spreadProgress, now))
      _ <- clearPending(chatId)
    } yield ()

  def clearSpread(chatId: Long): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logDebug(s"Clear spread for chat $chatId")

      now <- DateTimeService.getDateTimeNow
      _ <- botSessionRepository.update(chatId)(session => BotSession.clearSpread(session, now))
    } yield ()

  def clearSpreadProgress(chatId: Long): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logDebug(s"Clear spread progress for chat $chatId")

      now <- DateTimeService.getDateTimeNow
      _ <- botSessionRepository.update(chatId)(session => BotSession.clearSpreadProgress(session, now))
    } yield ()

  def setCard(chatId: Long, index: Int): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logDebug(s"Set card $index for chat $chatId")

      now <- DateTimeService.getDateTimeNow
      _ <- botSessionRepository.updateZIO(chatId)(session => BotSession.withCard(session, index, now))
      _ <- clearPending(chatId)
    } yield ()

  def setDate(chatId: Long, date: LocalDate): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logDebug(s"Set date $date for chat $chatId")

      now <- DateTimeService.getDateTimeNow
      _ <- botSessionRepository.update(chatId)(session => BotSession.withDate(session, date, now))
    } yield ()

  def setTime(chatId: Long, time: LocalTime): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logDebug(s"Set time $time for chat $chatId")

      now <- DateTimeService.getDateTimeNow
      _ <- botSessionRepository.update(chatId)(session => BotSession.withTime(session, time, now))
    } yield ()
}
