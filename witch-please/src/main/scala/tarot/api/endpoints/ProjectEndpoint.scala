package tarot.api.endpoints

import sttp.model.StatusCode
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import tarot.api.dto.common.IdResponse
import tarot.api.dto.tarot.TarotErrorResponse
import tarot.api.dto.tarot.projects.ProjectCreateRequest
import tarot.api.infrastructure.AuthValidator
import tarot.application.commands.*
import tarot.application.commands.projects.ProjectCreateCommand
import tarot.domain.models.authorize.{Role, UserId}
import tarot.layers.AppEnv
import zio.ZIO

import java.util.UUID

object ProjectEndpoint {
  private final val tag = "projects"

  private val postProjectEndpoint: ZServerEndpoint[AppEnv, Any] =
    endpoint
      .post
      .in(ApiPath.apiPath / "project")
      .in(jsonBody[ProjectCreateRequest])
      .out(jsonBody[IdResponse])
      .errorOut(
        oneOf[TarotErrorResponse](
          oneOfVariant(StatusCode.BadRequest, jsonBody[TarotErrorResponse]),
          oneOfVariant(StatusCode.NotFound, jsonBody[TarotErrorResponse]),
          oneOfVariant(StatusCode.InternalServerError, jsonBody[TarotErrorResponse]),
          oneOfVariant(StatusCode.Unauthorized, jsonBody[TarotErrorResponse])
        )
      )
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(AuthValidator.verifyToken(Role.PreProject))
      .serverLogic { tokenPayload => request =>
        (for {
          _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to create project: ${request.name}")
          externalProject <- ProjectCreateRequest.fromRequest(request)
          handler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.projectCreateCommandHandler)
          command = ProjectCreateCommand(externalProject, UserId(tokenPayload.userId))
          projectId <- handler.handle(command)
        } yield IdResponse(projectId.id))
          .mapError(err => TarotErrorResponse.toResponse(err))
      }

  val endpoints: List[ZServerEndpoint[AppEnv, Any]] =
    List(postProjectEndpoint)
}
