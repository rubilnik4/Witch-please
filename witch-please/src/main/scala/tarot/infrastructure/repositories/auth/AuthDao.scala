package tarot.infrastructure.repositories.auth

import io.getquill.*
import io.getquill.jdbczio.*
import tarot.domain.entities.*
import tarot.domain.models.spreads.{SpreadStatus, SpreadStatusUpdate}
import tarot.infrastructure.repositories.projects.ProjectTableNames
import zio.ZIO

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class AuthDao(quill: Quill.Postgres[SnakeCase]) {
  import quill.*
  import AuthQuillMappings.given

  def getUserProject(userId: UUID, projectId: UUID): ZIO[Any, SQLException, Option[UserProjectEntity]] =
    run(
      quote {
        userProjectTable
          .filter(up => up.userId == lift(userId) && up.projectId == lift(projectId))
          .take(1)
      }
    ).map(_.headOption)

  def getUserRole(userId: UUID, projectId: UUID): ZIO[Any, SQLException, Option[UserRoleEntity]] =
    run(
      quote {
        userProjectTable
          .join(userTable)
          .on((userProject, user) => userProject.userId == user.id)          
          .filter { case (userProject, _) =>
            userProject.userId == lift(userId) && userProject.projectId == lift(projectId)
          }
          .take(1)
          .map { case (userProject, user) =>
            UserRoleEntity(user, userProject.projectId, userProject.role)
          }
      }
    ).map(_.headOption)

  private inline def userTable = quote {
    querySchema[UserEntity](AuthTableNames.users)
  }

  private inline def projectTable = quote {
    querySchema[ProjectEntity](ProjectTableNames.projects)
  }

  private inline def userProjectTable = quote {
    querySchema[UserProjectEntity](AuthTableNames.userProjects)
  }
}
