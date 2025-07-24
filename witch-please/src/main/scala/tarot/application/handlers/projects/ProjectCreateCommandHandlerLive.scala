package tarot.application.handlers.projects

import tarot.application.commands.projects.ProjectCreateCommand
import tarot.domain.models.TarotError
import tarot.domain.models.auth.Role
import tarot.domain.models.projects.{Project, ProjectId}
import tarot.layers.AppEnv
import zio.ZIO

final class ProjectCreateCommandHandlerLive extends ProjectCreateCommandHandler {
  def handle(command: ProjectCreateCommand): ZIO[AppEnv, TarotError, ProjectId] = {
    val externalProject = command.externalProject
    for {
      _ <- ZIO.logInfo(s"Executing create project command for $externalProject")

      userRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository.userRepository)
      exists <- userRepository.existsUser(command.userId)
      _ <- ZIO.unless(exists) {
        ZIO.logError(s"User ${command.userId} not found for project create") *>
          ZIO.fail(TarotError.NotFound(s"User ${command.userId} not found"))
      }
      
      userProjectRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository.userProjectRepository)
      project <- Project.toDomain(externalProject)
      userProject <- userProjectRepository.createProjectWithRole(project, command.userId, Role.Admin)   
      
      _ <- ZIO.logInfo(s"Successfully user project created: $userProject")
    } yield userProject.projectId
  }
}
