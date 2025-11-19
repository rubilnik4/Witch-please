package tarot.infrastructure.repositories.users

import shared.models.tarot.authorize.Role
import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.entities.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.models.authorize.{Author, UserId, UserProject, UserRole}
import tarot.domain.models.projects.{Project, ProjectId}
import tarot.infrastructure.repositories.projects.ProjectDao
import zio.*

final class UserProjectRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends UserProjectRepository {
  private val projectDao = ProjectDao(quill)
  private val userProjectDao = UserProjectDao(quill)

  def createUserProject(userProject: UserProject): ZIO[Any, TarotError, UserProject] =
    for {
      _ <- ZIO.logDebug(s"Creating user project $userProject")

      userProject <- userProjectDao.insertUserProject(UserProjectEntity.toEntity(userProject))
        .tapError(e => ZIO.logErrorCause(s"Failed to create user project $userProject", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to create user project $userProject", e))
    } yield UserProjectEntity.toDomain(userProject)

  def createProjectWithRole(project: Project, userId: UserId, role: Role): ZIO[Any, TarotError, UserProject] =
    for {
      _ <- ZIO.logDebug(s"Creating project $project for user $userId")

      userProject <- quill.transaction {
        for {
          projectId <- projectDao.insertProject(ProjectEntity.toEntity(project))
          userProjectEntity = UserProjectEntity(userId.id, project.id.id, role)
          userProject <- userProjectDao.insertUserProject(userProjectEntity)
        } yield userProject
      }
        .tapError(e => ZIO.logErrorCause(s"Failed to create project $project for user $userId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to create project $project for user $userId", e.getCause))
    } yield UserProjectEntity.toDomain(userProject)

  def getUserProject(userId: UserId, projectId: ProjectId): ZIO[Any, TarotError, Option[UserProject]] =
    for {
      _ <- ZIO.logDebug(s"Getting userProject $userId for project $projectId")

      userProject <- userProjectDao.getUserProject(userId.id, projectId.id)
        .tapError(e =>  ZIO.logErrorCause(s"Failed to get userProject $userId for project $projectId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get userProject $userId for project $projectId", e))
    } yield userProject.map(UserProjectEntity.toDomain)

  def getProjects(userId: UserId): ZIO[Any, TarotError, List[Project]] =
    for {
      _ <- ZIO.logDebug(s"Getting userProject projects by userId $userId")

      projects <- userProjectDao.getProjects(userId.id)
        .tapError(e =>  ZIO.logErrorCause(s"Failed to get userProject projects by userId $userId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get projects by userId $userId", e))
    } yield projects.map(ProjectEntity.toDomain)
  
  def getUserRole(userId: UserId, projectId: ProjectId): ZIO[Any, TarotError, Option[UserRole]] =
    for {
      _ <- ZIO.logDebug(s"Getting userRole $userId for project $projectId")

      userRole <- userProjectDao.getUserRole(userId.id, projectId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to get userRole $userId for project $projectId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get userRole $userId for project $projectId", e))
    } yield userRole.map(UserRoleEntity.toDomain)

  override def getAuthors(minSpreads: Int): ZIO[Any, TarotError, List[Author]] =
    for {
      _ <- ZIO.logDebug(s"Getting authors by min spreads $minSpreads")

      authors <- userProjectDao.getAuthors(minSpreads)
        .tapError(e => ZIO.logErrorCause(s"Failed to get authors by min spreads $minSpreads", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get authors by min spreads $minSpreads", e))
    } yield authors  
}
