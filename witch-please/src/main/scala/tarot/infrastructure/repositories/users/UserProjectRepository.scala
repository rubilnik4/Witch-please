package tarot.infrastructure.repositories.users

import tarot.domain.models.TarotError
import tarot.domain.models.auth.{Role, User, UserId, UserProject, UserRole}
import tarot.domain.models.projects.{Project, ProjectId}
import tarot.layers.AppEnv
import zio.ZIO

trait UserProjectRepository {
  def createUserProject(userProject: UserProject): ZIO[Any, TarotError, UserProject]
  def createProjectWithRole(project: Project, userId: UserId, role: Role): ZIO[Any, TarotError, UserProject]
  def getUserProject(userId: UserId, projectId: ProjectId): ZIO[Any, TarotError, Option[UserProject]]
  def getUserRole(userId: UserId, projectId: ProjectId): ZIO[Any, TarotError, Option[UserRole]]
}
