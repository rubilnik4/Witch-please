package tarot.api.endpoints

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.common.IdResponse
import shared.api.dto.tarot.errors.TarotErrorResponse
import shared.api.dto.tarot.projects.ProjectCreateRequest
import shared.models.tarot.authorize.Role
import sttp.model.StatusCode
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import tarot.api.dto.tarot.errors.TarotErrorResponseMapper
import tarot.api.dto.tarot.projects.ProjectCreateRequestMapper
import tarot.api.endpoints.errors.TapirError
import tarot.api.infrastructure.AuthValidator
import tarot.application.commands.*
import tarot.application.commands.projects.ProjectCreateCommand
import tarot.domain.models.authorize.UserId
import tarot.layers.TarotEnv
import zio.ZIO

import java.util.UUID

object ProjectEndpoint {
  private final val tag = "projects"

  private val postProjectEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .post
      .in(TarotApiRoutes.apiPath / "project")
      .in(jsonBody[ProjectCreateRequest])
      .out(jsonBody[IdResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(AuthValidator.verifyToken(Role.PreProject))
      .serverLogic { tokenPayload => request =>
        (for {
          _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to create project: ${request.name}")
          externalProject <- ProjectCreateRequestMapper.fromRequest(request)
          handler <- ZIO.serviceWith[TarotEnv](_.tarotCommandHandler.projectCreateCommandHandler)
          command = ProjectCreateCommand(externalProject, UserId(tokenPayload.userId))
          projectId <- handler.handle(command)
        } yield IdResponse(projectId.id))
          .mapError(err => TarotErrorResponseMapper.toResponse(err))
      }

  val endpoints: List[ZServerEndpoint[TarotEnv, Any]] =
    List(postProjectEndpoint)
}
