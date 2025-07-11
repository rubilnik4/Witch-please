package tarot.domain.entities

import tarot.domain.models.auth.{Role, UserId, UserProject, UserRole}
import tarot.domain.models.projects.ProjectId

import java.util.UUID

final case class UserRoleEntity(
  user: UserEntity,
  projectId: UUID,
  role: Role
)

object UserRoleEntity {
  def toDomain(userRole: UserRoleEntity): UserRole =
    UserRole(
      user = UserEntity.toDomain(userRole.user),
      projectId = ProjectId(userRole.projectId),
      role = userRole.role
    )
}