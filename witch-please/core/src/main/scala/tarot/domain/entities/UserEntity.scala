package tarot.domain.entities

import shared.models.tarot.authorize.ClientType
import tarot.domain.models.authorize.*
import tarot.domain.models.users.{User, UserId}

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
  def toDomain(userEntity: UserEntity): User =
    User(
      id = UserId(userEntity.id),
      clientId = userEntity.clientId,
      name = userEntity.name,
      clientType = userEntity.clientType,
      secretHash = userEntity.secretHash,
      active = userEntity.active,
      createdAt = userEntity.createdAt
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
