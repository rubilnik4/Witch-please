package tarot.application.queries.channels

import shared.models.tarot.spreads.SpreadStatus
import tarot.domain.models.TarotError
import tarot.domain.models.channels.UserChannel
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.users.{Author, User, UserId}
import tarot.infrastructure.repositories.channels.UserChannelRepository
import tarot.infrastructure.repositories.users.*
import tarot.layers.TarotEnv
import zio.ZIO

final class UserChannelQueryHandlerLive(
  userChannelRepository: UserChannelRepository
) extends UserChannelQueryHandler {

  override def getUserChannel(userId: UserId): ZIO[TarotEnv, TarotError, UserChannel] =
    for {
      _ <- ZIO.logInfo(s"Executing get channel query by userId $userId")
      
      userChannelMaybe <- userChannelRepository.getUserChannel(userId)
      userChannel <- ZIO.fromOption(userChannelMaybe)
        .orElseFail(TarotError.NotFound(s"User channel by userId $userId not found"))
    } yield userChannel

  override def getUserChannelByProject(projectId: ProjectId): ZIO[TarotEnv, TarotError, UserChannel] =
    for {
      _ <- ZIO.logInfo(s"Executing get channel query by projectId $projectId")

      userChannelMaybe <- userChannelRepository.getUserChannelByProject(projectId)
      userChannel <- ZIO.fromOption(userChannelMaybe)
        .orElseFail(TarotError.NotFound(s"User channel by projectId $projectId not found"))
    } yield userChannel

  override def validateUserChannels(userId: UserId): ZIO[TarotEnv, TarotError, Unit]  =
    for {
      _ <- ZIO.logInfo(s"Executing validate channel query by userId $userId")

      exists <- userChannelRepository.existUserChannels(userId)
      _ <- ZIO.unless(exists) {
        ZIO.logError(s"User channels by userId $userId not found") *>
          ZIO.fail(TarotError.NotFound(s"User channels by userId $userId not found"))
      }
    } yield()
}