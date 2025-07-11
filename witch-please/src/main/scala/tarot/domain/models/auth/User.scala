package tarot.domain.models.auth

import java.time.Instant
import java.util.UUID

final case class User(
  id: UserId,
  clientType: ClientType,
  secretHash: String,
  active: Boolean,
  createdAt: Instant
)
