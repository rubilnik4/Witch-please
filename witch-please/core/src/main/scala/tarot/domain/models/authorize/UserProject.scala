package tarot.domain.models.authorize

import shared.models.tarot.authorize.Role
import tarot.domain.models.projects.{Project, ProjectId}

final case class UserProject(
  userId: UserId,
  projectId: ProjectId,
  role: Role
)
{
  override def toString: String = s"user $userId project $projectId"
}