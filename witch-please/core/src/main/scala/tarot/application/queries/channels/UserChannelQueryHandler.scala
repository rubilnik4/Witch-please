package tarot.application.queries.channels

import tarot.domain.models.TarotError
import tarot.domain.models.channels.UserChannel
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.users.UserId
import tarot.layers.TarotEnv
import zio.ZIO

trait UserChannelQueryHandler {
  def getUserChannel(userId: UserId): ZIO[TarotEnv, TarotError, UserChannel]
  def getUserChannelByProject(projectId: ProjectId): ZIO[TarotEnv, TarotError, UserChannel]
  def validateUserChannels(userId: UserId): ZIO[TarotEnv, TarotError, Unit]
}
 
