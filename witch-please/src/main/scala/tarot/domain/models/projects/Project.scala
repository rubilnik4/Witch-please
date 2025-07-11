package tarot.domain.models.projects

import java.time.Instant
import java.util.UUID

case class Project(
  id: ProjectId,
  name: String,
  createdAt: Instant,
)
