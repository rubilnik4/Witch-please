package tarot.infrastructure.services.auth

import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtZIOJson}
import tarot.api.dto.tarot.auth.TokenPayload
import tarot.domain.models.TarotError
import tarot.domain.models.auth.{ClientType, Role}
import tarot.infrastructure.services.common.DateTimeService
import zio.json.*
import zio.{Cause, UIO, ZIO}

object JwtService {
  def generateToken(clientType: ClientType, userId: String, projectId: String, role: Role,
                    serverSecret: String, expirationMinutes: Int): UIO[String] =
    for {
      now <- DateTimeService.getDateTimeNow
      payload = TokenPayload(clientType, projectId, role).toJson
      claim = JwtClaim(
        subject = Some(userId),
        content = payload,
        issuedAt = Some(now.getEpochSecond),
        expiration = Some(now.plusSeconds(expirationMinutes.toLong * 60).getEpochSecond)
      )
      token = JwtZIOJson.encode(claim, serverSecret, JwtAlgorithm.HS256)
    } yield token

  def validateToken(token: String, serverSecret: String): ZIO[Any, TarotError, TokenPayload] = {
    for {
      claim <- ZIO.fromTry(JwtZIOJson.decode(token, serverSecret, Seq(JwtAlgorithm.HS256)))
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
