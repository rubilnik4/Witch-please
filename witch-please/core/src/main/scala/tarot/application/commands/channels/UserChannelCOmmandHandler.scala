package tarot.application.commands.channels

import tarot.application.commands.channels.commands.*
import tarot.domain.models.TarotError
import tarot.domain.models.channels.UserChannelId
import tarot.domain.models.users.UserId
import tarot.layers.TarotEnv
import zio.ZIO

trait UserChannelCommandHandler {
  def createUserChannel(command: CreateUserChannelCommand): ZIO[TarotEnv, TarotError, UserChannelId]
  def updateUserChannel(command: UpdateUserChannelCommand): ZIO[TarotEnv, TarotError, Unit]
}
