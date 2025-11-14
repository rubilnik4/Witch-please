package tarot.application.commands.projects

import shared.models.tarot.authorize.Role
import tarot.domain.models.TarotError
import tarot.domain.models.authorize.UserId
import tarot.domain.models.projects.{ExternalProject, Project, ProjectId}
import tarot.infrastructure.repositories.users.*
import tarot.layers.TarotEnv
import zio.ZIO

final class ProjectCommandHandlerLive(
  userRepository: UserRepository, 
  userProjectRepository: UserProjectRepository
) 
  extends ProjectCommandHandler {
  def createProject(externalProject: ExternalProject, userId: UserId): ZIO[TarotEnv, TarotError, ProjectId] = {
    for {
        _ <- ZIO.logInfo(s"Executing create project command for $externalProject")
       
        exists <- userRepository.existsUser(userId)
        _ <- ZIO.unless(exists) {
          ZIO.logError(s"User $userId not found for project create") *>
            ZIO.fail(TarotError.NotFound(s"User $userId not found"))
        }
        
        project <- Project.toDomain(externalProject)
        userProject <- userProjectRepository.createProjectWithRole(project, userId, Role.Admin)

        _ <- ZIO.logInfo(s"Successfully user project created: $userProject")
      } yield userProject.projectId
    }
}
