package tarot.domain.models.authorize

import tarot.domain.models.projects.ProjectId

final case class UserRole(
  user: User,
  projectId: ProjectId,
  role: Role
)
