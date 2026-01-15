package bot.infrastructure.services.sessions

import bot.domain.models.session.*
import bot.layers.BotEnv
import zio.ZIO

import java.time.*
import java.util.UUID

trait BotSessionService {
  def get(chatId: Long): ZIO[BotEnv, Throwable, BotSession]
  def start(chatId: Long, username: String): ZIO[BotEnv, Throwable, BotSession]
  def setUser(chatId: Long, userId: UUID, token: String): ZIO[BotEnv, Throwable, Unit]
  def setPending(chatId: Long, pending: BotPendingAction): ZIO[BotEnv, Throwable, Unit]
  def clearPending(chatId: Long): ZIO[BotEnv, Throwable, Unit]
  def setSpread(chatId: Long, spreadId: UUID, spreadProgress: SpreadProgress): ZIO[BotEnv, Throwable, Unit]
  def clearSpread(chatId: Long): ZIO[BotEnv, Throwable, Unit]
  def clearSpreadProgress(chatId: Long): ZIO[BotEnv, Throwable, Unit]
  def setCard(chatId: Long, cardId: UUID): ZIO[BotEnv, Throwable, Unit]
  def clearCard(chatId: Long): ZIO[BotEnv, Throwable, Unit]
  def setCardPosition(chatId: Long, position: CardPosition): ZIO[BotEnv, Throwable, Unit]
  def deleteCardPosition(chatId: Long, cardId: UUID): ZIO[BotEnv, Throwable, Unit]
  def setCardOfDay(chatId: Long, cardOfDayId: UUID): ZIO[BotEnv, Throwable, Unit]
  def clearCardOfDay(chatId: Long): ZIO[BotEnv, Throwable, Unit]
  def setDate(chatId: Long, date: LocalDate): ZIO[BotEnv, Throwable, Unit]
  def setTime(chatId: Long, time: LocalTime): ZIO[BotEnv, Throwable, Unit]
  def setCardOfDayDelay(chatId: Long, delay: Duration): ZIO[BotEnv, Throwable, Unit]
  def clearDateTime(chatId: Long): ZIO[BotEnv, Throwable, Unit]
  def reset(chatId: Long): ZIO[BotEnv, Throwable, Unit]
}
