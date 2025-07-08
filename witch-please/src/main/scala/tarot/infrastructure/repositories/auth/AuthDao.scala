package tarot.infrastructure.repositories.auth

import io.getquill.*
import io.getquill.jdbczio.*
import tarot.domain.entities.*
import tarot.domain.models.spreads.{SpreadStatus, SpreadStatusUpdate}
import tarot.infrastructure.repositories.TarotTableNames
import zio.ZIO

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class AuthDao(quill: Quill.Postgres[SnakeCase]) {
  import quill.*

  def getByUserProjectId(userId: UUID, projectId: UUID): ZIO[Any, SQLException, Option[UserProjectEntity]] =
    run(
      quote {
        userProjectTable
          .join(userTable)
          .on((userProject, user) => userProject.userId == user.id)
          .join(projectTable)
          .on { case ((userProject, user), project) => userProject.projectId == project.id }
          .filter { case ((userProject, _), project) =>
            userProject.userId == lift(userId) && userProject.projectId == lift(projectId)
          }
          .take(1)
          .map { case ((userProject, user), project) =>
            UserProjectEntity(user, project, userProject.role)
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
    querySchema[UserProjectEntity](ProjectTableNames.userProjects)
  }
}
