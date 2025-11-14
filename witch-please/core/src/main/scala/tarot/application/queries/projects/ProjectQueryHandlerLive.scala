package tarot.application.queries.projects

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.{User, UserId}
import tarot.domain.models.projects.Project
import tarot.infrastructure.repositories.users.UserProjectRepository
import tarot.layers.TarotEnv
import zio.ZIO

final class ProjectQueryHandlerLive(userProjectRepository: UserProjectRepository) extends ProjectQueryHandler {
  def getProjects(userId: UserId): ZIO[TarotEnv, TarotError, List[Project]] =
    for {
      _ <- ZIO.logInfo(s"Executing projects query by userId $userId")
      
      projects <- userProjectRepository.getProjects(userId)
    } yield projects
}