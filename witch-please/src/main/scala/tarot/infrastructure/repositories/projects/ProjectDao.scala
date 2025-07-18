package tarot.infrastructure.repositories.projects

import io.getquill.*
import io.getquill.jdbczio.*
import tarot.domain.entities.*
import tarot.infrastructure.repositories.TarotTableNames
import zio.ZIO

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class ProjectDao(quill: Quill.Postgres[SnakeCase]) {
  import quill.*

  def insertProject(project: ProjectEntity): ZIO[Any, SQLException, UUID] =
    run(
      quote {
        projectTable
          .insertValue(lift(project))
          .returning(_.id)
      })

  private inline def projectTable =
    quote(querySchema[ProjectEntity](TarotTableNames.projects))

}
