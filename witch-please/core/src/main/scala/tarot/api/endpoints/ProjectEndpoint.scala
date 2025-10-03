package tarot.api.endpoints

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.common.IdResponse
import shared.api.dto.tarot.projects.{ProjectCreateRequest, ProjectResponse}
import shared.models.tarot.authorize.Role
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import tarot.api.dto.tarot.errors.TarotErrorResponseMapper
import tarot.api.dto.tarot.projects.{ProjectCreateRequestMapper, ProjectResponseMapper}
import tarot.api.endpoints.errors.TapirError
import tarot.api.infrastructure.AuthValidator
import tarot.application.commands.*
import tarot.application.commands.projects.ProjectCreateCommand
import tarot.application.queries.projects.ProjectsQuery
import tarot.domain.models.authorize.UserId
import tarot.layers.TarotEnv
import zio.ZIO

import java.util.UUID

object ProjectEndpoint {
  import TapirError.*
  
  private final val tag = "projects"

  private val getProjectsEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .get
      .in(TarotApiRoutes.apiPath / "project" / "by-user" / path[UUID]("userId"))
      .out(jsonBody[List[ProjectResponse]])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.PreProject)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        userId =>
          (for {
            _ <- ZIO.logInfo(s"Received request to get projects by userId $userId")

            handler <- ZIO.serviceWith[TarotEnv](_.tarotQueryHandler.projectsQueryHandler)
            query = ProjectsQuery(UserId(userId))
            projects <- handler.handle(query)
          } yield projects.map(ProjectResponseMapper.toResponse)).mapResponseErrors
      }

  private val postProjectEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .post
      .in(TarotApiRoutes.apiPath / "project")
      .in(jsonBody[ProjectCreateRequest])
      .out(jsonBody[IdResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.PreProject)(token).mapResponseErrors)
      .serverLogic { tokenPayload => request =>
        (for {
          _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to create project: ${request.name}")

          externalProject <- ProjectCreateRequestMapper.fromRequest(request)
          handler <- ZIO.serviceWith[TarotEnv](_.tarotCommandHandler.projectCreateCommandHandler)
          command = ProjectCreateCommand(externalProject, UserId(tokenPayload.userId))
          projectId <- handler.handle(command)
        } yield IdResponse(projectId.id)).mapResponseErrors
      }

  val endpoints: List[ZServerEndpoint[TarotEnv, Any]] =
    List(getProjectsEndpoint, postProjectEndpoint)
}
