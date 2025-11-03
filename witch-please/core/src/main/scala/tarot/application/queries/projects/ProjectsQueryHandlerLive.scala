package tarot.application.queries.projects

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.{User, UserId}
import tarot.domain.models.projects.Project
import tarot.layers.TarotEnv
import zio.ZIO

final class ProjectsQueryHandlerLive extends ProjectsQueryHandler {
  def getProjects(userId: UserId): ZIO[TarotEnv, TarotError, List[Project]] =
    for {
      _ <- ZIO.logInfo(s"Executing projects query by userId $userId")

      repository <- ZIO.serviceWith[TarotEnv](_.tarotRepository.userProjectRepository)
      projects <- repository.getProjects(userId)
    } yield projects
}