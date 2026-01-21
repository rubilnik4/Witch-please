package tarot.application.commands.channels.commands

import shared.models.tarot.authorize.ClientType
import tarot.domain.models.users.UserId

import java.util.UUID

final case class CreateUserChannelCommand(
  userId: UserId,
  chatId: Long,
  name: String
)