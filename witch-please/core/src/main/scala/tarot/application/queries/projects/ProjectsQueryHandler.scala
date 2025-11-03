package tarot.application.queries.projects

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.{User, UserId}
import tarot.domain.models.projects.Project
import tarot.layers.TarotEnv
import zio.ZIO

trait ProjectsQueryHandler {
  def getProjects(userId: UserId): ZIO[TarotEnv, TarotError, List[Project]]
}
  
