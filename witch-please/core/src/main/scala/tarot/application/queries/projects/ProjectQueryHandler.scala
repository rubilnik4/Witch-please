package tarot.application.queries.projects

import tarot.domain.models.TarotError
import tarot.domain.models.projects.{Project, ProjectId}
import tarot.domain.models.users.{User, UserId}
import tarot.layers.TarotEnv
import zio.ZIO

trait ProjectQueryHandler {
  def getProjects(userId: UserId): ZIO[TarotEnv, TarotError, List[Project]]
  def getDefaultProject(userId: UserId): ZIO[TarotEnv, TarotError, ProjectId]
}
  
