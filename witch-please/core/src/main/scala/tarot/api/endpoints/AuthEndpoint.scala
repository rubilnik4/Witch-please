package tarot.api.endpoints

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.authorize.{AuthRequest, AuthResponse}
import shared.models.tarot.authorize.Role
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import tarot.api.dto.tarot.*
import tarot.api.dto.tarot.authorize.AuthResponseMapper
import tarot.api.dto.tarot.errors.TarotErrorResponseMapper
import tarot.api.endpoints.errors.TapirError
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.users.UserId
import tarot.layers.TarotEnv
import zio.ZIO

object AuthEndpoint {
  private final val tag = "auth"

  private val postAuthEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .post
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.auth)
      .in(jsonBody[AuthRequest])
      .out(jsonBody[AuthResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .zServerLogic { request =>
        (for {
          _ <- ZIO.logInfo(s"Received request to auth user: ${request.userId}")
          
          authService <- ZIO.serviceWith[TarotEnv](_.services.authService)
          token <- authService.issueToken(request.clientType, UserId(request.userId), request.clientSecret)
        } yield AuthResponseMapper.fromDomain(token))
          .mapError(TarotErrorResponseMapper.toResponse)
      }

  val endpoints: List[ZServerEndpoint[TarotEnv, Any]] = 
    List(postAuthEndpoint)
}
