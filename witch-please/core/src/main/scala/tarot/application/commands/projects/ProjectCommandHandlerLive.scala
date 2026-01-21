package tarot.application.commands.projects

import shared.models.tarot.authorize.Role
import tarot.domain.models.TarotError
import tarot.domain.models.projects.{ExternalProject, Project, ProjectId}
import tarot.domain.models.users.UserId
import tarot.infrastructure.repositories.users.*
import tarot.layers.TarotEnv
import zio.ZIO

final class ProjectCommandHandlerLive(
  userProjectRepository: UserProjectRepository
) extends ProjectCommandHandler {
  override def createDefaultProject(userId: UserId): ZIO[TarotEnv, TarotError, ProjectId] =
    for {
      _ <- ZIO.logInfo(s"Create default project for user $userId")

      externalProject = ExternalProject("initial project")
      project <- Project.toDomain(externalProject)
      userProject <- userProjectRepository.createProjectWithRole(project, userId, Role.Admin)
    } yield userProject.projectId
}
