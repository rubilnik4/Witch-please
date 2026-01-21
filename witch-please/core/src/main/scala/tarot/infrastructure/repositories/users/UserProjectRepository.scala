package tarot.infrastructure.repositories.users

import shared.models.tarot.authorize.Role
import tarot.domain.models.TarotError
import tarot.domain.models.projects.{Project, ProjectId}
import tarot.domain.models.users.{Author, User, UserId, UserProject, UserRole}
import tarot.layers.TarotEnv
import zio.ZIO

trait UserProjectRepository {
  def createUserProject(userProject: UserProject): ZIO[Any, TarotError, UserProject]
  def createProjectWithRole(project: Project, userId: UserId, role: Role): ZIO[Any, TarotError, UserProject]
  def getUserProject(userId: UserId, projectId: ProjectId): ZIO[Any, TarotError, Option[UserProject]]
  def getProjects(userId: UserId): ZIO[Any, TarotError, List[Project]]
  def getProjectIds(userId: UserId): ZIO[Any, TarotError, List[ProjectId]]
  def getUserRole(userId: UserId, projectId: ProjectId): ZIO[Any, TarotError, Option[UserRole]]
  def getAuthors(minSpreads: Int): ZIO[Any, TarotError, List[Author]]
}
