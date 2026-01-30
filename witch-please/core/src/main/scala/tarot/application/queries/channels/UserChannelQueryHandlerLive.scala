package tarot.application.queries.channels

import shared.models.tarot.spreads.SpreadStatus
import tarot.domain.models.TarotError
import tarot.domain.models.channels.{UserChannel, UserChannelId}
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.users.{Author, User, UserId}
import tarot.infrastructure.repositories.channels.UserChannelRepository
import tarot.infrastructure.repositories.users.*
import tarot.layers.TarotEnv
import zio.ZIO

final class UserChannelQueryHandlerLive(
  userChannelRepository: UserChannelRepository
) extends UserChannelQueryHandler {

  override def getUserChannel(userChannelId: UserChannelId): ZIO[TarotEnv, TarotError, UserChannel] =
    for {
      _ <- ZIO.logInfo(s"Executing get channel query by user channel $userChannelId")

      userChannelMaybe <- userChannelRepository.getUserChannel(userChannelId)
      userChannel <- ZIO.fromOption(userChannelMaybe)
        .orElseFail(TarotError.NotFound(s"User channel $userChannelId not found"))
    } yield userChannel
    
  override def getUserChannelByUser(userId: UserId): ZIO[TarotEnv, TarotError, Option[UserChannel]] =
    for {
      _ <- ZIO.logInfo(s"Executing get channel query by user $userId")

      userChannel <- userChannelRepository.getUserChannelByUser(userId)     
    } yield userChannel

  override def getUserChannelByProject(projectId: ProjectId): ZIO[TarotEnv, TarotError, UserChannel] =
    for {
      _ <- ZIO.logInfo(s"Executing get channel query by project $projectId")

      userChannelMaybe <- userChannelRepository.getUserChannelByProject(projectId)
      userChannel <- ZIO.fromOption(userChannelMaybe)
        .orElseFail(TarotError.NotFound(s"User channel by project $projectId not found"))
    } yield userChannel

  override def validateUserChannels(userId: UserId): ZIO[TarotEnv, TarotError, Unit]  =
    for {
      _ <- ZIO.logInfo(s"Executing validate channel query by user $userId")

      exists <- userChannelRepository.existUserChannels(userId)
      _ <- ZIO.unless(exists) {
        ZIO.logError(s"User channels by userId $userId not found") *>
          ZIO.fail(TarotError.NotFound(s"User channels by userId $userId not found"))
      }
    } yield()

}