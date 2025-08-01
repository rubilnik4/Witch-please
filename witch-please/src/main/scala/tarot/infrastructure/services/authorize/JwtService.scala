package tarot.infrastructure.services.authorize

import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtOptions, JwtZIOJson}
import tarot.api.dto.tarot.authorize.TokenPayload
import tarot.domain.models.TarotError
import tarot.domain.models.authorize.{ClientType, Role}
import tarot.infrastructure.services.common.DateTimeService
import zio.json.*
import zio.{Cause, UIO, ZIO}

import java.util.UUID

object JwtService {
  def generateToken(clientType: ClientType, userId: UUID, projectId: Option[UUID], role: Role,
                    serverSecret: String, expirationMinutes: Int): UIO[String] =
    for {
      now <- DateTimeService.getDateTimeNow
      payload = TokenPayload(clientType, userId, projectId, role).toJson
      claim = JwtClaim(
        subject = Some(userId.toString),
        content = payload,
        issuedAt = Some(now.getEpochSecond),
        expiration = Some(now.plusSeconds(expirationMinutes.toLong * 60).getEpochSecond)
      )
      token = JwtZIOJson.encode(claim, serverSecret, JwtAlgorithm.HS256)
    } yield token

  def validateToken(token: String, serverSecret: String): ZIO[Any, TarotError, TokenPayload] = {
    for {
      claim <- ZIO.fromTry(JwtZIOJson.decode(token, serverSecret, Seq(JwtAlgorithm.HS256),
          options = JwtOptions.DEFAULT.copy(expiration = false)))
        .tapError(err => ZIO.logErrorCause(s"Can't decode token", Cause.fail(err)))
        .mapError(err => TarotError.Unauthorized(s"Can't decode token: $err"))

      now <- DateTimeService.getDateTimeNow.map(_.getEpochSecond)
      _ <- ZIO.when(claim.expiration.exists(_ < now)) {
        ZIO.logWarning(s"Unauthorized token: token expired for user ${claim.subject}") *>
          ZIO.fail(TarotError.Unauthorized("Token expired"))
      }

      payload <- ZIO.fromEither(claim.content.fromJson[TokenPayload])
        .tapError(err => ZIO.logErrorCause(s"Invalid token payload for user ${claim.subject}", Cause.fail(err)))
        .mapError(err => TarotError.Unauthorized(s"Invalid token payload for user ${claim.subject}"))
    } yield payload
  }
}
