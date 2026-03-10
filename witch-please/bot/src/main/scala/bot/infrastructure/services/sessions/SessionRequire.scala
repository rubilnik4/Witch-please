package bot.infrastructure.services.sessions

import bot.domain.models.session.*
import bot.domain.models.session.pending.BotPending
import bot.layers.BotEnv
import zio.ZIO

import java.time.*

object SessionRequire {
  def session(chatId: Long): ZIO[BotEnv, Throwable, BotSession] =
    for {
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      session <- sessionService.get(chatId)
        .orElseFail(new RuntimeException(s"Session not found for chat $chatId"))
    } yield session
  
  def token(chatId: Long): ZIO[BotEnv, Throwable, String] =
    for {
      session <- session(chatId)
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat $chatId"))
    } yield token

  def spread(chatId: Long): ZIO[BotEnv, Throwable, BotSpread] =
    for {
      session <- session(chatId)
      spread <- ZIO.fromOption(session.spread)
        .orElseFail(new RuntimeException(s"Spread not found in session for chat $chatId"))
    } yield spread

  def spreadProgress(chatId: Long): ZIO[BotEnv, Throwable, SpreadProgress] =
    for {
      session <- session(chatId)
      progress <- ZIO.fromOption(session.spreadProgress)
        .orElseFail(new RuntimeException("Spread progress not found"))
    } yield progress

  def cardsCount(chatId: Long): ZIO[BotEnv, Throwable, Int] =
    for {
      session <- session(chatId)
      cardsCount <- ZIO.fromOption(session.spreadProgress.map(_.cardsCount))
        .orElseFail(new RuntimeException(s"Cards count not found in session for chat $chatId"))
    } yield cardsCount

  def date(chatId: Long): ZIO[BotEnv, Throwable, LocalDate] =
    for {
      session <- session(chatId)
      date <- ZIO.fromOption(session.date)
        .orElseFail(new RuntimeException(s"Date not found in session for chat $chatId"))
    } yield date

  def time(chatId: Long): ZIO[BotEnv, Throwable, LocalTime] =
    for {
      session <- session(chatId)
      date <- ZIO.fromOption(session.time)
        .orElseFail(new RuntimeException(s"Time not found in session for chat $chatId"))
    } yield date

  def cardOfDayDelay(chatId: Long): ZIO[BotEnv, Throwable, Duration] =
    for {
      session <- session(chatId)
      date <- ZIO.fromOption(session.cardOfDayDelay)
        .orElseFail(new RuntimeException(s"Card of day delay not found in session for chat $chatId"))
    } yield date

  def pending(chatId: Long): ZIO[BotEnv, Throwable, BotPending] =
    for {
      session <- session(chatId)
      pending <- ZIO.fromOption(session.pending)
        .orElseFail(new RuntimeException(s"Pending not found in session for chat $chatId"))
    } yield pending  
}
