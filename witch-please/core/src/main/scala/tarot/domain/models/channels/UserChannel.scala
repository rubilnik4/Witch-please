package tarot.domain.models.channels

import shared.infrastructure.services.common.DateTimeService
import tarot.application.commands.channels.commands.CreateUserChannelCommand
import tarot.application.commands.users.commands.CreateAuthorCommand
import tarot.domain.models.users.{User, UserId}
import zio.UIO

import java.time.Instant
import java.util.UUID

final case class UserChannel(
  id: UserChannelId,
  userId: UserId,
  channelId: Long,
  name: String,
  isDefault: Boolean,
  createdAt: Instant
)

object UserChannel {
  def toDomain(command: CreateUserChannelCommand): UIO[UserChannel] =
    for {
      createdAt <- DateTimeService.getDateTimeNow
      userChannel = UserChannel(
        id = UserChannelId(UUID.randomUUID()),
        userId = command.userId,
        channelId = command.channelId,
        name = command.name,
        isDefault = true,
        createdAt = createdAt)
    } yield userChannel
}