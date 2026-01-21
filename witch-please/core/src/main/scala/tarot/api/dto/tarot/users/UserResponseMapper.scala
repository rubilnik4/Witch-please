package tarot.api.dto.tarot.users

import shared.api.dto.tarot.users.*
import shared.models.tarot.authorize.ClientType
import tarot.application.commands.users.commands.CreateAuthorCommand
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.users.User
import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

object UserResponseMapper { 
  def toResponse(user: User): UserResponse =
    UserResponse(
      id = user.id.id,
      clientId = user.clientId,
      clientType = user.clientType,
      name = user.name,
      createdAt = user.createdAt,
    )
}