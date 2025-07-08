package tarot.domain.entities

import java.util.UUID

final case class UserProjectEntity(
  userId: UUID,
  projectId: UUID,
  role: UserRole
)
