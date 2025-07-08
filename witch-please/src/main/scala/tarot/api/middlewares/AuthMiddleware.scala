package tarot.api.middlewares

import tarot.domain.models.auth.UserRole
import tarot.layers.AppEnv
import zio.http.HandlerAspect

object AuthMiddleware {
  def requireRole(requiredRole: UserRole): HandlerAspect[AppEnv, TokenPayload] =
    HandlerAspect.interceptIncomingHandler(Handler.fromFunctionZIO[Request] { request =>
      for {
        header <- ZIO.fromOption(request.header(Header.Authorization))
          .orElseFail(Response.unauthorized("Missing Authorization header"))

        token <- header match {
          case Header.Authorization.Bearer(value) => ZIO.succeed(value.toString)
          case _ => ZIO.fail(Response.unauthorized("Invalid Authorization header format"))
        }

        payload <- AuthService.validateToken(token)
          .mapError(err => Response.unauthorized(s"Token validation failed: ${err.message}"))

        _ <- ZIO.when(payload.role != requiredRole) {
          ZIO.fail(Response.forbidden(s"Insufficient role: required $requiredRole, got ${payload.role}"))
        }

      } yield (request, payload)
    })
}
