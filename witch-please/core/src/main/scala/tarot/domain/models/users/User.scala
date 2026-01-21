package tarot.domain.models.users

import shared.infrastructure.services.common.DateTimeService
import shared.models.tarot.authorize.ClientType
import tarot.application.commands.users.commands.CreateAuthorCommand
import zio.UIO

import java.time.Instant
import java.util.UUID

final case class User(
  id: UserId,
  clientId: String,
  clientType: ClientType,
  name: String,
  secretHash: String,
  active: Boolean,
  createdAt: Instant
)

object User {
  def toDomain(command: CreateAuthorCommand, secretHash: String): UIO[User] =
    for {
      createdAt <- DateTimeService.getDateTimeNow
      user = User(
        id = UserId(UUID.randomUUID()),
        clientId = command.clientId,
        name = command.name,
        clientType = command.clientType,
        secretHash = secretHash,
        active = true,
        createdAt = createdAt)
    } yield user
}