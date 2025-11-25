package bot.infrastructure.services.sessions

import bot.domain.models.session.*
import bot.layers.BotEnv
import zio.ZIO

import java.time.{LocalDate, LocalTime}
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
  def setCard(chatId: Long, index: Int): ZIO[BotEnv, Throwable, Unit]
  def setDate(chatId: Long, date: LocalDate): ZIO[BotEnv, Throwable, Unit]
  def setTime(chatId: Long, time: LocalTime): ZIO[BotEnv, Throwable, Unit]
  def reset(chatId: Long): ZIO[BotEnv, Throwable, Unit]
}
