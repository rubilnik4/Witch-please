package tarot.infrastructure.services.auth

import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtZIOJson}
import tarot.api.dto.tarot.auth.TokenPayload
import tarot.domain.models.TarotError
import tarot.infrastructure.services.common.DateTimeService
import zio.json.*
import zio.{UIO, ZIO}

object JwtService {
  def generateToken(clientId: String, role: String, secretKey: String, expirationMinutes: Int): UIO[String] =
    for {
      now <- DateTimeService.getDateTimeNow
      payload = TokenPayload(clientId, role).toJson
      claim = JwtClaim(
        content = payload,
        issuedAt = Some(now.getEpochSecond),
        expiration = Some(now.plusSeconds(expirationMinutes.toLong * 60).getEpochSecond)
      )
      token = JwtZIOJson.encode(claim, secretKey, JwtAlgorithm.HS256)
    } yield token

  def validateToken(token: String, secretKey: String): ZIO[Any, TarotError, TokenPayload] = {
    for {
      claim <- ZIO.fromTry(JwtZIOJson.decode(token, secretKey, Seq(JwtAlgorithm.HS256)))
        .mapError(err => TarotError.Unauthorized(s"Can't decode token: $err"))

      now <- DateTimeService.getDateTimeNow.map(_.getEpochSecond)
      _ <- ZIO.when(claim.expiration.exists(_ < now)) {
        ZIO.fail(TarotError.Unauthorized("Token expired"))
      }

      payload <- ZIO.fromEither(claim.content.fromJson[TokenPayload])
        .mapError(err => TarotError.Unauthorized(s"Invalid token payload: $err"))
    } yield payload
  }
}
