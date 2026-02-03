package tarot.application.queries.channels

import tarot.domain.models.TarotError
import tarot.domain.models.channels.{UserChannel, UserChannelId}
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.users.UserId
import tarot.layers.TarotEnv
import zio.ZIO

trait UserChannelQueryHandler {
  def getUserChannel(userChannelId: UserChannelId): ZIO[TarotEnv, TarotError, UserChannel]
  def getUserChannelByUser(userId: UserId): ZIO[TarotEnv, TarotError, Option[UserChannel]]
  def getUserChannelByProject(projectId: ProjectId): ZIO[TarotEnv, TarotError, UserChannel]
  def validateUserChannels(userId: UserId): ZIO[TarotEnv, TarotError, Unit]
  def validateChannel(channelId: Long): ZIO[TarotEnv, TarotError, Unit]
}
 
