package tarot.domain.entities

import tarot.domain.models.projects.*

import java.time.Instant
import java.util.UUID

final case class ProjectEntity(
  id: UUID,
  name: String,
  createdAt: Instant
)

object ProjectEntity {
  def toDomain(project: ProjectEntity): Project =
    Project(
      id = ProjectId(project.id),
      name = project.name,
      createdAt = project.createdAt
    )

  def toEntity(project: Project): ProjectEntity =
    ProjectEntity(
      id = project.id.id,
      name = project.name,
      createdAt = project.createdAt
    )
}