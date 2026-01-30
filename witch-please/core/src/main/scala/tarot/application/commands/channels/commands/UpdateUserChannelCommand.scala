package tarot.application.commands.channels.commands

import shared.models.tarot.authorize.ClientType
import tarot.domain.models.channels.UserChannelId
import tarot.domain.models.users.UserId

import java.util.UUID

final case class UpdateUserChannelCommand(
  userChannelId: UserChannelId,
  channelId: Long,
  name: String
)