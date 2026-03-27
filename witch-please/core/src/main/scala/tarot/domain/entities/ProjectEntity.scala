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
  def toDomain(projectEntity: ProjectEntity): Project =
    Project(
      id = ProjectId(projectEntity.id),
      name = projectEntity.name,
      createdAt = projectEntity.createdAt
    )

  def toEntity(project: Project): ProjectEntity =
    ProjectEntity(
      id = project.id.id,
      name = project.name,
      createdAt = project.createdAt
    )
}
