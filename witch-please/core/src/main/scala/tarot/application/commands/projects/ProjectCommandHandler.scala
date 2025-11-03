package tarot.application.commands.projects

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.UserId
import tarot.domain.models.projects.{ExternalProject, ProjectId}
import tarot.layers.TarotEnv
import zio.ZIO

trait ProjectCommandHandler {
  def createProject(externalProject: ExternalProject, userId: UserId): ZIO[TarotEnv, TarotError, ProjectId]
} 
