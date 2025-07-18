package tarot.api.middlewares

import tarot.api.dto.tarot.TarotErrorResponse
import tarot.api.dto.tarot.auth.TokenPayload
import tarot.domain.models.TarotError
import tarot.domain.models.auth.Role
import tarot.layers.AppEnv
import zio.ZIO
import zio.http.{Handler, HandlerAspect, Header, Request}

object AuthMiddleware {
  def requireRole(requiredRole: Role): HandlerAspect[AppEnv, TokenPayload] =
    HandlerAspect.interceptIncomingHandler(
      Handler.fromFunctionZIO[Request] { request =>
        (for {
            header <- ZIO.fromOption(request.header(Header.Authorization))
              .orElseFail(TarotError.Unauthorized("Missing authorization header"))

            token <- header match {
              case Header.Authorization.Bearer(v) => ZIO.succeed(v.toString)
              case _ => ZIO.fail(TarotError.Unauthorized("Invalid authorization header format"))
            }

            authService <- ZIO.serviceWith[AppEnv](_.tarotService.authService)
            payload <- authService.validateToken(token)

            _ <- ZIO.when(payload.role != requiredRole) {
              ZIO.fail(TarotError.Unauthorized(s"Required $requiredRole, got ${payload.role}"))
            }
          } yield (request, payload))
          .mapError(TarotErrorResponse.toHttpResponse)
      }
    )
}
