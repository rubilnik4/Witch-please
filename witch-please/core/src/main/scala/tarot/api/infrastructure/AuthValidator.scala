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
  def verifyToken(requiredRole: Role)(token: String): ZIO[TarotEnv, TarotErrorResponse, TokenPayload] =
    (for {
      authService <- ZIO.serviceWith[TarotEnv](_.tarotService.authService)
      payload <- authService.validateToken(token)
      _ <- ZIO.when(payload.role != requiredRole) {
        ZIO.fail(TarotError.Unauthorized(s"Required $requiredRole, got ${payload.role}"))
      }
    } yield payload)
      .mapError(TarotErrorResponseMapper.toResponse)
}
