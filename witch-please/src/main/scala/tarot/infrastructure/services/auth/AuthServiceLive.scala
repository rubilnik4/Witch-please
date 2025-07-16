package tarot.infrastructure.services.auth

import tarot.domain.models.TarotError
import tarot.domain.models.auth.{ClientType, ExternalUser, Role, User, UserId, UserProject, UserRole}
import tarot.layers.AppEnv
import zio.{Cause, ZIO}
import com.github.roundrop.bcrypt.*
import tarot.api.dto.tarot.auth.TokenPayload
import tarot.domain.models.projects.ProjectId

final case class AuthServiceLive() extends AuthService { 
  def issueToken(clientType: ClientType, userId: UserId, projectId: ProjectId, clientSecret: String)
      : ZIO[AppEnv, TarotError, String] = {
    for {
      _ <- ZIO.logDebug(s"Attempting to get auth token for user $userId")

      userRole <- getUserRole(userId, projectId)
      isValid <- ZIO.fromTry(clientSecret.isBcrypted(userRole.user.secretHash))
        .tapError(err => ZIO.logErrorCause(s"Decryption error for user $userId", Cause.fail(err)))
        .mapError(err => TarotError.Unauthorized(s"Decryption error for user $userId"))

      _ <- ZIO.unless(isValid)(
        ZIO.logWarning(s"Unauthorized token request: invalid secret for userId=$userId, project=$projectId") *>
          ZIO.fail(TarotError.Unauthorized(s"Invalid client secret for user $userId"))
      )

      config <- ZIO.serviceWith[AppEnv](_.appConfig.jwt)
      token <- JwtService.generateToken(
        clientType = clientType,
        userId = userId.id.toString,
        projectId = projectId.id.toString,
        role = userRole.role,
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

  private def getUserRole(userId: UserId, projectId: ProjectId): ZIO[AppEnv, TarotError, UserRole] =
    for {
      userAccessRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository.userAccessRepository)
      userProject <- userAccessRepository.getUserRole(userId, projectId).flatMap {
        case Some(userProject) => ZIO.succeed(userProject)
        case None =>
          ZIO.logWarning(s"Authorization failed: user not found for userId=$userId, project=$projectId") *>
            ZIO.fail(TarotError.Unauthorized(s"User $userId not found"))
      }
    } yield userProject
}