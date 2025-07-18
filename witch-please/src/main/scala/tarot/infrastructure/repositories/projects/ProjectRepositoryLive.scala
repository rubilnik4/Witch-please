package tarot.infrastructure.repositories.projects

import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.entities.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.models.projects.{Project, ProjectId}
import tarot.layers.AppEnv
import zio.*

final class ProjectRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends ProjectRepository {
  private val projectDao = ProjectDao(quill)

  def createProject(project: Project): ZIO[Any, TarotError, ProjectId] =
    projectDao
      .insertProject(ProjectEntity.toEntity(project))
      .mapBoth(
        e => DatabaseError(s"Failed to create project $project", e),
        ProjectId(_))
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to create project $project to database", Cause.fail(e.ex)),
        _ => ZIO.logDebug(s"Successfully create project $project to database")
      )
}
