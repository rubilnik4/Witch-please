package tarot.domain.models.users

import shared.models.tarot.authorize.Role
import tarot.domain.models.projects.ProjectId

final case class UserRole(
  user: User,
  projectId: ProjectId,
  role: Role
)
