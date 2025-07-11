package tarot.domain.entities

import tarot.domain.models.auth.{ClientType, User, UserId, Role}

import java.time.Instant
import java.util.UUID

final case class UserEntity(
  id: UUID,
  clientType: ClientType,
  secretHash: String,
  active: Boolean,
  createdAt: Instant
)

object UserEntity {
  def toDomain(user: UserEntity): User =
    User(
      id = UserId(user.id),
      clientType = user.clientType,
      secretHash = user.secretHash,
      active = user.active,
      createdAt = user.createdAt
    )

  def toEntity(user: User): UserEntity =
    UserEntity(
      id = user.id.id,
      clientType = user.clientType,
      secretHash = user.secretHash,
      active = user.active,
      createdAt = user.createdAt
    )
}