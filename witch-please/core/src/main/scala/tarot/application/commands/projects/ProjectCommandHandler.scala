package tarot.application.commands.projects

import tarot.domain.models.TarotError
import tarot.domain.models.projects.{ExternalProject, ProjectId}
import tarot.domain.models.users.UserId
import tarot.layers.TarotEnv
import zio.ZIO

trait ProjectCommandHandler {
  def createDefaultProject(userId: UserId): ZIO[TarotEnv, TarotError, ProjectId]
} 
