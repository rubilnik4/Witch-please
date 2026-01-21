package tarot.infrastructure.repositories.channels

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.*
import tarot.domain.models.channels.{UserChannel, UserChannelId}
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.users.{User, UserId}
import tarot.layers.TarotEnv
import zio.ZIO

trait UserChannelRepository {
  def createUserChannel(userChannel: UserChannel): ZIO[Any, TarotError, UserChannelId]
  def getUserChannel(userId: UserId): ZIO[TarotEnv, TarotError, Option[UserChannel]]
}
