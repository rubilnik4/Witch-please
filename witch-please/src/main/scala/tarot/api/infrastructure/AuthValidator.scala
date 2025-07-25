package tarot.api.infrastructure

import tarot.api.dto.tarot.TarotErrorResponse
import tarot.api.dto.tarot.authorize.TokenPayload
import tarot.domain.models.TarotError
import tarot.domain.models.auth.Role
import tarot.layers.AppEnv
import zio.ZIO
import zio.http.{Handler, HandlerAspect, Header, Request}

object AuthValidator {
  def verifyToken(requiredRole: Role)(token: String): ZIO[AppEnv, TarotErrorResponse, TokenPayload] =
    (for {
      authService <- ZIO.serviceWith[AppEnv](_.tarotService.authService)
      payload <- authService.validateToken(token)
      _ <- ZIO.when(payload.role != requiredRole) {
        ZIO.fail(TarotError.Unauthorized(s"Required $requiredRole, got ${payload.role}"))
      }
    } yield payload)
      .mapError(TarotErrorResponse.toResponse)
}
