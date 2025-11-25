package tarot.application.queries.projects

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.{User, UserId}
import tarot.domain.models.projects.{Project, ProjectId}
import tarot.infrastructure.repositories.users.UserProjectRepository
import tarot.layers.TarotEnv
import zio.ZIO

final class ProjectQueryHandlerLive(userProjectRepository: UserProjectRepository) extends ProjectQueryHandler {
  override def getProjects(userId: UserId): ZIO[TarotEnv, TarotError, List[Project]] =
    for {
      _ <- ZIO.logInfo(s"Executing projects query by userId $userId")
      
      projects <- userProjectRepository.getProjects(userId)
    } yield projects

  override def getDefaultProject(userId: UserId): ZIO[TarotEnv, TarotError, ProjectId]  =
    for {
      _ <- ZIO.logInfo(s"Executing default project query for user $userId")

      projectIds <- userProjectRepository.getProjectIds(userId)
      projectId <- ZIO.fromOption(projectIds.headOption)
        .orElseFail(TarotError.NotFound(s"No project found for user $userId"))
    } yield projectId
}