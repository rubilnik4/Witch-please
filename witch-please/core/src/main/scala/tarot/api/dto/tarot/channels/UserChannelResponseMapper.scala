package tarot.api.dto.tarot.channels

import shared.api.dto.tarot.channels.UserChannelResponse
import shared.api.dto.tarot.users.*
import shared.models.tarot.authorize.ClientType
import tarot.application.commands.users.commands.CreateAuthorCommand
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.channels.UserChannel
import tarot.domain.models.users.User
import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

object UserChannelResponseMapper {
  def toResponse(userChannel: UserChannel): UserChannelResponse =
    UserChannelResponse(
      id = userChannel.id.id,
      channelId = userChannel.channelId,
      name = userChannel.name,
      createdAt = userChannel.createdAt,
    )
}