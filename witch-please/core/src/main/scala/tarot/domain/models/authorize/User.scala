package tarot.domain.models.authorize

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
{
  override def toString: String = id.toString
}

object User {
  def toDomain(externalUser: CreateAuthorCommand, secretHash: String): UIO[User] =
    for {
      createdAt <- DateTimeService.getDateTimeNow
      user = User(
        id = UserId(UUID.randomUUID()),
        clientId = externalUser.clientId,
        name = externalUser.name,
        clientType = externalUser.clientType,
        secretHash = secretHash,
        active = true,
        createdAt = createdAt)
    } yield user
}