package tarot.infrastructure.repositories.projects

import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.entities.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.models.projects.{Project, ProjectId}
import tarot.domain.models.users.UserId
import tarot.layers.TarotEnv
import zio.*

final class ProjectRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends ProjectRepository {
  private val projectDao = ProjectDao(quill)
      
  def createProject(project: Project): ZIO[Any, TarotError, ProjectId] =
    for {
      _ <- ZIO.logDebug(s"Creating project $project")

      projectId <- projectDao.insertProject(ProjectEntity.toEntity(project))
        .tapError(e => ZIO.logErrorCause(s"Failed to create project $project", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to create project $project", e))
    } yield ProjectId(projectId)
}
