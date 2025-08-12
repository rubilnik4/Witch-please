package tarot.api.endpoints

import shared.api.dto.tarot.authorize.{AuthRequest, AuthResponse}
import shared.api.dto.tarot.errors.TarotErrorResponse
import shared.models.tarot.authorize.{ClientType, Role}
import shared.models.tarot.contracts.TarotChannelType
import sttp.model.StatusCode
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.*
import tarot.api.dto.tarot.*
import tarot.api.dto.tarot.authorize.*
import tarot.api.dto.tarot.errors.TarotErrorResponseMapper
import tarot.api.dto.tarot.users.UserCreateRequestMapper
import tarot.api.infrastructure.AuthValidator
import tarot.application.commands.*
import tarot.application.commands.spreads.{CardCreateCommand, SpreadCreateCommand, SpreadPublishCommand}
import tarot.application.commands.users.UserCreateCommand
import tarot.domain.models.authorize.{AuthResponseMapper, UserId}
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.SpreadId
import tarot.layers.AppEnv
import zio.ZIO

object AuthEndpoint {
  private final val tag = "auth"

  val postAuthEndpoint: ZServerEndpoint[AppEnv, Any] =
    endpoint
      .post
      .in(ApiPath.apiPath / "auth")
      .in(jsonBody[AuthRequest])
      .out(jsonBody[AuthResponse])
      .errorOut(
        oneOf[TarotErrorResponse](
          oneOfVariant(StatusCode.BadRequest, jsonBody[TarotErrorResponse]),
          oneOfVariant(StatusCode.InternalServerError, jsonBody[TarotErrorResponse])
        )
      )
      .tag(tag)
      .zServerLogic { request =>
        (for {
          _ <- ZIO.logInfo(s"Received request to auth user: ${request.userId}")
          authService <- ZIO.serviceWith[AppEnv](_.tarotService.authService)
          token <- authService.issueToken(request.clientType, UserId(request.userId), request.clientSecret,
            request.projectId.map(ProjectId(_)))
        } yield AuthResponseMapper.fromDomain(token))
          .mapError(TarotErrorResponseMapper.toResponse)
      }

  val endpoints: List[ZServerEndpoint[AppEnv, Any]] = 
    List(postAuthEndpoint)
}
