package tarot.infrastructure.repositories.projects

import tarot.domain.models.TarotError
import tarot.domain.models.projects.{Project, ProjectId}
import tarot.layers.TarotEnv
import zio.ZIO

trait ProjectRepository {
  def createProject(project: Project): ZIO[Any, TarotError, ProjectId]
}
