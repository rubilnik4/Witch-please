package tarot.infrastructure.services.auth

import tarot.domain.models.TarotError
import tarot.domain.models.auth.{ClientType, User, UserRole}
import tarot.layers.AppEnv
import zio.{Cause, ZIO}
import com.github.roundrop.bcrypt.*
import tarot.api.dto.tarot.auth.TokenPayload

final case class TokenIssuerServiceLive() extends TokenIssuerService {
  def issueToken(clientType: ClientType, userId: String, projectId: String, clientSecret: String)
      : ZIO[AppEnv, TarotError, String] = {
    for {
      _ <- ZIO.logDebug(s"Attempting to get auth token for user $userId")

      user <- getUser(userId, projectId)
      isValid <- ZIO.fromTry(clientSecret.isBcrypted(user.secretHash))
        .tapError(err => ZIO.logErrorCause(s"Decryption error for user $userId", Cause.fail(err)))
        .mapError(err => TarotError.Unauthorized(s"Decryption error for user $userId"))

      _ <- ZIO.unless(isValid)(
        ZIO.logWarning(s"Unauthorized token request: invalid secret for userId=$userId, project=$projectId") *>
          ZIO.fail(TarotError.Unauthorized(s"Invalid client secret for user $userId"))
      )

      config <- ZIO.serviceWith[AppEnv](_.appConfig.jwt)
      token <- JwtService.generateToken(
        clientType = clientType,
        userId = userId,
        projectId = projectId,
        role = user.role,
        serverSecret = config.secret,
        expirationMinutes = config.expirationMinutes
      )
    } yield token
  }

  def validateToken(token: String): ZIO[AppEnv, TarotError, TokenPayload] = {
    for {
      _ <- ZIO.logDebug(s"Attempting to validate token")

      config <- ZIO.serviceWith[AppEnv](_.appConfig.jwt)
      tokenPayload <- JwtService.validateToken(token, config.secret)
    } yield tokenPayload
  }

  private def getUser(userId: String, projectId: String): ZIO[AppEnv, TarotError, User] =
    for {
      authRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository.authRepository)
      user <- authRepository.getByUserId(userId, projectId).flatMap {
        case Some(user) => ZIO.succeed(user)
        case None =>
          ZIO.logWarning(s"Authorization failed: user not found for userId=$userId, project=$projectId") *>
            ZIO.fail(TarotError.Unauthorized(s"User $userId not found"))
      }
    } yield user
}