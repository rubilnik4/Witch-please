package tarot.domain.entities

import tarot.domain.models.auth.*

import java.time.Instant
import java.util.UUID

final case class UserEntity(
  id: UUID,
  name: String,
  clientType: ClientType,
  clientId: String,
  secretHash: String,
  active: Boolean,
  createdAt: Instant
)

object UserEntity {
  def toDomain(user: UserEntity): User =
    User(
      id = UserId(user.id),
      clientId = user.clientId,
      name = user.name,
      clientType = user.clientType,
      secretHash = user.secretHash,
      active = user.active,
      createdAt = user.createdAt
    )

  def toEntity(user: User): UserEntity =
    UserEntity(
      id = user.id.id,
      clientId = user.clientId,
      name = user.name,
      clientType = user.clientType,
      secretHash = user.secretHash,
      active = user.active,
      createdAt = user.createdAt
    )
}