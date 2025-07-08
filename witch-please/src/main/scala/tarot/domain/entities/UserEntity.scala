package tarot.domain.entities

import tarot.domain.models.auth.{ClientType, UserRole}

final case class UserEntity(
  userId: UUID,
  clientType: ClientType,
  secretHash: String,
  active: Boolean,
  createdAt: Instant
)
