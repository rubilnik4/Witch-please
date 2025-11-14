package tarot.application.queries.projects

import tarot.infrastructure.repositories.projects.ProjectRepository
import tarot.infrastructure.repositories.users.UserProjectRepository
import zio.ZLayer

object ProjectQueryHandlerLayer {
  val live: ZLayer[UserProjectRepository, Nothing, ProjectQueryHandler] =
    ZLayer.fromFunction(new ProjectQueryHandlerLive(_))
}
