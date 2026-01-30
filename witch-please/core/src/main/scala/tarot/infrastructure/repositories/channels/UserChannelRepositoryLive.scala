package tarot.infrastructure.repositories.channels

import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.entities.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.models.channels.{UserChannel, UserChannelId, UserChannelUpdate}
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.users.UserId
import tarot.layers.TarotEnv
import zio.*

final class UserChannelRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends UserChannelRepository {
  private val userChannelDao = UserChannelDao(quill)

  override def createUserChannel(userChannel: UserChannel): ZIO[Any, TarotError, UserChannelId] =
    for {
      _ <- ZIO.logDebug(s"Creating channel ${userChannel.channelId} for user ${userChannel.userId}")

      userChannelId <- userChannelDao.insertUserChannel(UserChannelEntity.toEntity(userChannel))
        .tapError(e => ZIO.logErrorCause(s"Failed to create user channel ${userChannel.id}", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to create user channel ${userChannel.id}", e))
    } yield UserChannelId(userChannelId)

  override def updateUserChannel(userChannelId: UserChannelId, userChannelUpdate: UserChannelUpdate): ZIO[Any, TarotError, Unit] =
    for {
      _ <- ZIO.logDebug(s"Updating channel ${userChannelUpdate.channelId} for user channel $userChannelId")

      userChannelId <- userChannelDao.updateUserChannel(userChannelId.id, userChannelUpdate)
        .tapError(e => ZIO.logErrorCause(s"Failed to update user channel $userChannelId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to update user channel $userChannelId", e))
    } yield ()

  override def getUserChannel(userChannelId: UserChannelId): ZIO[TarotEnv, TarotError, Option[UserChannel]] =
    for {
      _ <- ZIO.logDebug(s"Getting user channel by user channel $userChannelId")

      userChannel <- userChannelDao.getUserChannel(userChannelId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to get user channel $userChannelId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get user channel $userChannelId", e))
    } yield userChannel.map(UserChannelEntity.toDomain)
    
  override def getUserChannelByUser(userId: UserId): ZIO[TarotEnv, TarotError, Option[UserChannel]] =
    for {
      _ <- ZIO.logDebug(s"Getting user channel by user $userId")

      userChannel <- userChannelDao.getUserChannelByUser(userId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to get user channel by user $userId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get user channel by user $userId", e))
    } yield userChannel.map(UserChannelEntity.toDomain)

  override def getUserChannelByProject(projectId: ProjectId): ZIO[TarotEnv, TarotError, Option[UserChannel]] =
    for {
      _ <- ZIO.logDebug(s"Getting user channel by project $projectId")

      userChannel <- userChannelDao.getUserChannelByProject(projectId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to get user channel by project $projectId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get user channel by project $projectId", e))
    } yield userChannel.map(UserChannelEntity.toDomain)
    
  override def existUserChannels(userId: UserId): ZIO[TarotEnv, TarotError, Boolean] =
    for {
      _ <- ZIO.logDebug(s"Checking channels exists by user $userId")

      exists <- userChannelDao.existsUserChannels(userId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to check user channels by user $userId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to check user channels channel by user $userId", e))
    } yield exists

}
