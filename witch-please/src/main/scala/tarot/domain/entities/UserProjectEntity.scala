package tarot.domain.entities

import tarot.domain.models.auth.{User, UserId, UserProject, Role}
import tarot.domain.models.projects.{Project, ProjectId}

import java.util.UUID

final case class UserProjectEntity(
  userId: UUID,
  projectId: UUID,
  role: Role
)

object UserProjectEntity {
  def toDomain(userProject: UserProjectEntity): UserProject =
    UserProject(
      userId = UserId(userProject.userId),
      projectId = ProjectId(userProject.projectId),
      role = userProject.role
    )

  def toEntity(userProject: UserProject): UserProjectEntity =
    UserProjectEntity(
      userId = userProject.userId.id,
      projectId = userProject.projectId.id,
      role = userProject.role
    )
}