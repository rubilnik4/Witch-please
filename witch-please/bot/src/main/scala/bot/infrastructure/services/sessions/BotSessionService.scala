package bot.infrastructure.services.sessions

import zio.UIO

import java.util.UUID

trait BotSessionService {
  def touch(chatId: Long): UIO[Unit]
  def setProject(chatId: Long, projectId: UUID): UIO[Unit]
}
