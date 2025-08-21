package bot.infrastructure.services.sessions

import bot.domain.models.session.*
import bot.layers.AppEnv
import zio.{UIO, ZIO}

import java.util.UUID

trait BotSessionService {
  def get(chatId: Long): ZIO[AppEnv, Nothing, BotSession]
  def start(chatId: Long): ZIO[AppEnv, Nothing, BotSession]
  def setUser(chatId: Long, userId: UUID, token: String): ZIO[AppEnv, Nothing, Unit]
  def setPending(chatId: Long, pending: BotPendingAction): ZIO[AppEnv, Nothing, Unit]
  def clearPending(chatId: Long): ZIO[AppEnv, Nothing, Unit]
  def setProject(chatId: Long, projectId: UUID, token: String): ZIO[AppEnv, Nothing, Unit]
  def reset(chatId: Long): ZIO[AppEnv, Nothing, Unit]
}
