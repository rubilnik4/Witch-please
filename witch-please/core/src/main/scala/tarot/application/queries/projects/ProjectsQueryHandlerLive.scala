package tarot.application.queries.projects

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.User
import tarot.domain.models.projects.Project
import tarot.layers.TarotEnv
import zio.ZIO

final class ProjectsQueryHandlerLive extends ProjectsQueryHandler {
  def handle(query: ProjectsQuery): ZIO[TarotEnv, TarotError, List[Project]] =
    for {
      _ <- ZIO.logInfo(s"Executing projects query by userId ${query.userId}")

      repository <- ZIO.serviceWith[TarotEnv](_.tarotRepository.userProjectRepository)
      projects <- repository.getProjects(query.userId)
    } yield projects
}