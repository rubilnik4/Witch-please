package tarot.infrastructure.repositories.channels

import tarot.domain.models.TarotError
import tarot.domain.models.channels.{UserChannel, UserChannelId, UserChannelUpdate}
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.users.UserId
import tarot.layers.TarotEnv
import zio.ZIO

trait UserChannelRepository {
  def createUserChannel(userChannel: UserChannel): ZIO[Any, TarotError, UserChannelId]
  def updateUserChannel(userChannelId: UserChannelId, userChannelUpdate: UserChannelUpdate): ZIO[Any, TarotError, Unit]
  def getUserChannel(userChannelId: UserChannelId): ZIO[TarotEnv, TarotError, Option[UserChannel]]
  def getUserChannelByUser(userId: UserId): ZIO[TarotEnv, TarotError, Option[UserChannel]]
  def getUserChannelByProject(projectId: ProjectId): ZIO[TarotEnv, TarotError, Option[UserChannel]]
  def existUserChannels(userId: UserId): ZIO[TarotEnv, TarotError, Boolean]
}
