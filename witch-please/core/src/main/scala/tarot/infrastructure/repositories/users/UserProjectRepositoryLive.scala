package tarot.infrastructure.repositories.users

import shared.models.tarot.authorize.Role
import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.entities.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.models.authorize.{UserId, UserProject, UserRole}
import tarot.domain.models.projects.{Project, ProjectId}
import tarot.infrastructure.repositories.projects.ProjectDao
import zio.*

final class UserProjectRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends UserProjectRepository {
  private val projectDao = ProjectDao(quill)
  private val userProjectDao = UserProjectDao(quill)

  def createUserProject(userProject: UserProject): ZIO[Any, TarotError, UserProject] =
    userProjectDao
      .insertUserProject(UserProjectEntity.toEntity(userProject))
      .mapBoth(
        e => DatabaseError(s"Failed to create user project $userProject", e),
        entity => UserProjectEntity.toDomain(entity))
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to create user project $userProject to database", Cause.fail(e.ex)),
        _ => ZIO.logDebug(s"Successfully create user project $userProject to database")
      )

  def createProjectWithRole(project: Project, userId: UserId, role: Role): ZIO[Any, TarotError, UserProject] =
    quill.transaction {
      val projectEntity = ProjectEntity.toEntity(project)
      for {
        projectId <- projectDao.insertProject(projectEntity)
        userProjectEntity = UserProjectEntity(userId.id, project.id.id, role)
        userProject <- userProjectDao.insertUserProject(userProjectEntity)
      } yield UserProjectEntity.toDomain(userProject)
    }
    .mapError(e => DatabaseError(s"Failed to create project $project for user $userId", e.getCause))
    .tapBoth(
      e => ZIO.logErrorCause(s"Failed to create project $project for user $userId to database", Cause.fail(e.ex)),
      _ => ZIO.logDebug(s"Successfully create project $project for user $userId to database")
    )

  def getUserProject(userId: UserId, projectId: ProjectId): ZIO[Any, TarotError, Option[UserProject]] =
    userProjectDao
      .getUserProject(userId.id, projectId.id)
      .mapError(e => DatabaseError(s"Failed to get userProject $userId for project $projectId", e))
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to get userProject $userId for project $projectId from database", Cause.fail(e.ex)),
        _ => ZIO.logDebug(s"Successfully get userProject $userId for project $projectId from database")
      ).flatMap {
        case Some(userProject) =>
          ZIO.some(UserProjectEntity.toDomain(userProject))
        case None =>
          ZIO.none
      }

  def getUserRole(userId: UserId, projectId: ProjectId): ZIO[Any, TarotError, Option[UserRole]] =
    userProjectDao
      .getUserRole(userId.id, projectId.id)
      .mapError(e => DatabaseError(s"Failed to get userRole $userId for project $projectId", e))
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to get userRole $userId for project $projectId from database", Cause.fail(e.ex)),
        _ => ZIO.logDebug(s"Successfully get userRole $userId for project $projectId from database")
      ).map(_.map(UserRoleEntity.toDomain))
}
