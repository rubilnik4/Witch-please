package bot.infrastructure.services.sessions

import bot.layers.AppEnv
import shared.infrastructure.services.common.DateTimeService
import zio.ZIO

import java.util.UUID

final class BotSessionServiceLive extends BotSessionService {
  def touch(chatId: Long): ZIO[AppEnv, Nothing, Unit] =
    for {
      now <- DateTimeService.getDateTimeNow
      botSessionRepository <- ZIO.serviceWith[AppEnv](_.botRepository.botSessionRepository)
      _ <- botSessionRepository.update(chatId)(identity, now)
    } yield ()

  def setProject(chatId: Long, projectId: UUID): ZIO[AppEnv, Nothing, Unit] =
    for {
      now <- DateTimeService.getDateTimeNow
      botSessionRepository <- ZIO.serviceWith[AppEnv](_.botRepository.botSessionRepository)
      _ <- botSessionRepository.update(chatId)(session => session.copy(projectId = Some(projectId)), now)
    } yield ()
}
