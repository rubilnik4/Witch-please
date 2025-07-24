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
import tarot.domain.models.auth.{ClientType, Role, TokenPayload}
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

object ProjectEndpoint {
  private val postProjectEndpoint =
    endpoint
      .post
      .in("api" / "project")
      .in(jsonBody[ProjectCreateRequest])
      .out(stringBody)
      .errorOut(
        oneOf[TarotErrorResponse](
          oneOfVariant(StatusCode.BadRequest, jsonBody[TarotErrorResponse].description("Bad Request")),
          oneOfVariant(StatusCode.NotFound, jsonBody[TarotErrorResponse].description("Not Found")),
          oneOfVariant(StatusCode.InternalServerError, jsonBody[TarotErrorResponse].description("Internal Server Error")),
          oneOfVariant(StatusCode.Unauthorized, jsonBody[TarotErrorResponse].description("Unauthorized"))
        )
      )
      .tag("projects")
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic { token =>
        AuthValidator.verifyToken(Role.PreProject)(token)
      }
      .serverLogic { tokenPayload => request =>
        (for {
          _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to create project: ${request.name}")
          externalProject <- ProjectCreateRequest.fromRequest(request)
          handler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.projectCreateCommandHandler)
          cmd = ProjectCreateCommand(externalProject, tokenPayload.userId)
          id <- handler.handle(cmd)
        } yield id.id.toString)
          .mapError(err => TarotErrorResponse.toResponse(err))
      }

  val endpoints: List[ZServerEndpoint[AppEnv, Any]] =
    List(postProjectEndpoint)
}
