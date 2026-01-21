package tarot.application.queries.channels

import tarot.domain.models.TarotError
import tarot.domain.models.channels.UserChannel
import tarot.domain.models.spreads.Spread
import tarot.domain.models.users.{Author, User, UserId}
import tarot.layers.TarotEnv
import zio.ZIO

trait UserChannelQueryHandler {
  def getDefaultUserChannel(userId: UserId): ZIO[TarotEnv, TarotError, UserChannel]
}
 
