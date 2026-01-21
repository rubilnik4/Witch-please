package tarot.infrastructure.repositories.channels

import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.entities.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.models.channels.{UserChannel, UserChannelId}
import tarot.domain.models.users.{Author, User, UserId}
import tarot.layers.TarotEnv
import zio.*

final class UserChannelRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends UserChannelRepository {
  private val userChannelDao = UserChannelDao(quill)

  override def createUserChannel(userChannel: UserChannel): ZIO[Any, TarotError, UserChannelId] =
    for {
      _ <- ZIO.logDebug(s"Creating channel ${userChannel.chatId} for user ${userChannel.userId}")

      userChannelId <- userChannelDao.insertUserChannel(UserChannelEntity.toEntity(userChannel))
        .tapError(e => ZIO.logErrorCause(s"Failed to create user channel ${userChannel.id}", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to create user channel ${userChannel.id}", e))
    } yield UserChannelId(userChannelId)
    
  override def getUserChannel(userId: UserId): ZIO[TarotEnv, TarotError, Option[UserChannel]] =
    for {
      _ <- ZIO.logDebug(s"Getting default channel by user $userId")

      userChannel <- userChannelDao.getUserChannel(userId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to get user channel by user $userId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get user channel by user $userId", e))
    } yield userChannel.map(UserChannelEntity.toDomain)
}
