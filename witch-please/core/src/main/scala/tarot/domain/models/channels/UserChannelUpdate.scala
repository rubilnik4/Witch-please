package tarot.domain.models.channels

import tarot.application.commands.channels.commands.UpdateUserChannelCommand

final case class UserChannelUpdate(
  channelId: Long,
  name: String
)

object UserChannelUpdate {
  def toDomain(command: UpdateUserChannelCommand): UserChannelUpdate =
    UserChannelUpdate(
      channelId = command.channelId,
      name = command.name
    )
}
