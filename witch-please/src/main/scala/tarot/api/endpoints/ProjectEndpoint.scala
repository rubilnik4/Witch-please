package tarot.api.endpoints

import sttp.model.StatusCode
import tarot.api.dto.tarot.projects.ProjectCreateRequest
import tarot.api.dto.tarot.spreads.*
import tarot.api.dto.tarot.users.UserCreateRequest
import tarot.api.infrastructure.AuthValidator
import tarot.application.commands.*
import tarot.application.commands.projects.ProjectCreateCommand
import tarot.application.commands.spreads.{CardCreateCommand, SpreadCreateCommand, SpreadPublishCommand}
import tarot.application.commands.users.UserCreateCommand
import tarot.domain.models.auth.{ClientType, Role, UserId}
import tarot.domain.models.contracts.TarotChannelType
import tarot.domain.models.spreads.SpreadId
import tarot.layers.AppEnv
import zio.ZIO
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.generic.auto.*
import tarot.api.dto.tarot.TarotErrorResponse
import tarot.api.dto.tarot.authorize.TokenPayload

import java.util.UUID

object ProjectEndpoint {
  private final val projectTag = "projects"

  private val postProjectEndpoint: ZServerEndpoint[AppEnv, Any] =
    endpoint
      .post
      .in(ApiPath.apiPath / "project")
      .in(jsonBody[ProjectCreateRequest])
      .out(jsonBody[UUID])
      .errorOut(
        oneOf[TarotErrorResponse](
          oneOfVariant(StatusCode.BadRequest, jsonBody[TarotErrorResponse]),
          oneOfVariant(StatusCode.NotFound, jsonBody[TarotErrorResponse]),
          oneOfVariant(StatusCode.InternalServerError, jsonBody[TarotErrorResponse]),
          oneOfVariant(StatusCode.Unauthorized, jsonBody[TarotErrorResponse])
        )
      )
      .tag(projectTag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(AuthValidator.verifyToken(Role.PreProject))
      .serverLogic { tokenPayload => request =>
        (for {
          _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to create project: ${request.name}")
          externalProject <- ProjectCreateRequest.fromRequest(request)
          handler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.projectCreateCommandHandler)
          command = ProjectCreateCommand(externalProject, UserId(tokenPayload.userId))
          projectId <- handler.handle(command)
        } yield projectId.id)
          .mapError(err => TarotErrorResponse.toResponse(err))
      }

  val endpoints: List[ZServerEndpoint[AppEnv, Any]] =
    List(postProjectEndpoint)
}
