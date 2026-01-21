package tarot.api.dto.tarot.channels

import shared.api.dto.tarot.channels.ChannelCreateRequest
import shared.api.dto.tarot.users.*
import shared.models.tarot.authorize.ClientType
import tarot.application.commands.channels.commands.CreateUserChannelCommand
import tarot.application.commands.users.commands.CreateAuthorCommand
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.users.UserId
import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

object ChannelCreateRequestMapper {
  def fromRequest(request: ChannelCreateRequest, userId: UserId): IO[TarotError, CreateUserChannelCommand] = {
    for {
      _ <- ZIO.fail(ValidationError("chatId must be positive number")).when(request.chatId <= 0)
      _ <- ZIO.fail(ValidationError("name must not be empty")).when(request.name.isEmpty)
    } yield toDomain(request, userId)
  }

  private def toDomain(request: ChannelCreateRequest, userId: UserId): CreateUserChannelCommand =
    CreateUserChannelCommand(
      userId = userId,
      chatId = request.chatId,
      name = request.name
    )
}