package tarot.domain.entities

import java.util.UUID

final case class ProjectEntity(
  id: UUID,
  name: String,
  createdAt: Instant
)
