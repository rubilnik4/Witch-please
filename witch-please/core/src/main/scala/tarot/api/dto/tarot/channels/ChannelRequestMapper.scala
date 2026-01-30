package tarot.api.dto.tarot.channels

import shared.api.dto.tarot.channels.*
import shared.api.dto.tarot.users.*
import shared.models.tarot.authorize.ClientType
import tarot.application.commands.cards.commands.CreateCardCommand
import tarot.application.commands.channels.commands.{CreateUserChannelCommand, UpdateUserChannelCommand}
import tarot.application.commands.users.commands.CreateAuthorCommand
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.channels.UserChannelId
import tarot.domain.models.spreads.SpreadId
import tarot.domain.models.users.UserId
import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

object ChannelRequestMapper {
  def fromRequest(request: ChannelCreateRequest, userId: UserId): IO[TarotError, CreateUserChannelCommand] =
    validate(request).as(toCreateCommand(request, userId))

  def fromRequest(request: ChannelUpdateRequest, userChannelId: UserChannelId): IO[TarotError, UpdateUserChannelCommand] =
    validate(request).as(toUpdateCommand(request, userChannelId))  

  private def toCreateCommand(request: ChannelCreateRequest, userId: UserId): CreateUserChannelCommand =
    CreateUserChannelCommand(
      userId = userId,
      channelId = request.channelId,
      name = request.name
    )

  private def toUpdateCommand(request: ChannelUpdateRequest, userChannelId: UserChannelId): UpdateUserChannelCommand =
    UpdateUserChannelCommand(
      userChannelId = userChannelId,
      channelId = request.channelId,
      name = request.name
    )  

  private def validate(request: ChannelRequest) = {
    for {
      _ <- ZIO.fail(ValidationError("chatId must be positive number")).when(request.channelId <= 0)
      _ <- ZIO.fail(ValidationError("name must not be empty")).when(request.name.isEmpty)
    } yield ()
  }  
}