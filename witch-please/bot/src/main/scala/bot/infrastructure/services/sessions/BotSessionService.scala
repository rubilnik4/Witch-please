package bot.infrastructure.services.sessions

import bot.domain.models.session.*
import bot.layers.AppEnv
import zio.{UIO, ZIO}

import java.util.UUID

trait BotSessionService {
  def start(chatId: Long): ZIO[AppEnv, Nothing, BotSession]
  def setProject(chatId: Long, projectId: UUID): ZIO[AppEnv, Nothing, Unit]
  def reset(chatId: Long): ZIO[AppEnv, Nothing, Unit]
}
