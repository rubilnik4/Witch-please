package tarot.domain.entities

import shared.models.tarot.authorize.ClientType
import tarot.domain.models.authorize.*
import tarot.domain.models.channels.{UserChannel, UserChannelId}
import tarot.domain.models.users.{User, UserId}

import java.time.Instant
import java.util.UUID

final case class UserChannelEntity(
  id: UUID,
  userId: UUID,
  channelId: Long,
  name: String,
  isDefault: Boolean,
  createdAt: Instant
)

object UserChannelEntity {
  def toDomain(userChannel: UserChannelEntity): UserChannel =
    UserChannel(
      id = UserChannelId(userChannel.id),
      userId = UserId(userChannel.userId),
      channelId = userChannel.channelId,
      name = userChannel.name,
      isDefault = userChannel.isDefault,
      createdAt = userChannel.createdAt
    )

  def toEntity(userChannel: UserChannel): UserChannelEntity =
    UserChannelEntity(
      id = userChannel.id.id,
      userId = userChannel.userId.id,
      channelId = userChannel.channelId,
      name = userChannel.name,
      isDefault = userChannel.isDefault,
      createdAt = userChannel.createdAt
    )
}