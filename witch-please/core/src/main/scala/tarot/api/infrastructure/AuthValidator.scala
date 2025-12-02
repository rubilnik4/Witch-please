package tarot.api.infrastructure

import shared.api.dto.tarot.errors.TarotErrorResponse
import shared.models.tarot.authorize.Role
import tarot.api.dto.tarot.authorize.TokenPayload
import tarot.api.dto.tarot.errors.TarotErrorResponseMapper
import tarot.domain.models.TarotError
import tarot.layers.TarotEnv
import zio.ZIO
import zio.http.{Handler, HandlerAspect, Header, Request}

object AuthValidator {
  def verifyToken(requiredRole: Role)(token: String): ZIO[TarotEnv, TarotError, TokenPayload] =
    for {
      authService <- ZIO.serviceWith[TarotEnv](_.services.authService)
      payload <- authService.validateToken(token)
      _ <- ZIO.unless(Role.atLeast(payload.role, requiredRole)) {
        ZIO.logWarning(s"Authorization for user ${payload.userId} failed: required $requiredRole, got ${payload.role}") *>
          ZIO.fail(TarotError.Unauthorized(s"Dor user ${payload.userId} required $requiredRole, got ${payload.role}"))
      }
    } yield payload      
}
