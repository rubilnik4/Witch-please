package tarot.domain.entities

import shared.models.tarot.authorize.Role
import tarot.domain.models.authorize.{User, UserId, UserProject}
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