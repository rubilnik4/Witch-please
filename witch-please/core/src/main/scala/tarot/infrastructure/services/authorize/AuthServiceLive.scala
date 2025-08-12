package tarot.infrastructure.services.authorize

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.{ExternalUser, Token, User, UserId, UserProject, UserRole}
import tarot.layers.AppEnv
import zio.{Cause, ZIO}
import com.github.roundrop.bcrypt.*
import shared.models.tarot.authorize.{ClientType, Role}
import tarot.api.dto.tarot.authorize.TokenPayload
import tarot.domain.models.projects.ProjectId

final case class AuthServiceLive() extends AuthService { 
  def issueToken(clientType: ClientType, userId: UserId, clientSecret: String, projectId: Option[ProjectId])
      : ZIO[AppEnv, TarotError, Token] = {
    for {
      _ <- ZIO.logDebug(s"Attempting to get auth token for user $userId")
      
      user <- getUser(userId)
      isValid <- ZIO.fromTry(clientSecret.isBcrypted(user.secretHash))
        .tapError(err => ZIO.logErrorCause(s"Decryption error for user $userId", Cause.fail(err)))
        .mapError(err => TarotError.Unauthorized(s"Decryption error for user $userId"))

      _ <- ZIO.unless(isValid)(
        ZIO.logWarning(s"Unauthorized token request: invalid secret for userId=$userId, project=$projectId") *>
          ZIO.fail(TarotError.Unauthorized(s"Invalid client secret for user $userId"))
      )

      role <- getUserRole(userId, projectId)
      config <- ZIO.serviceWith[AppEnv](_.appConfig.jwt)
      token <- JwtService.generateToken(
        clientType = clientType,
        userId = userId.id,
        projectId = projectId.map(_.id),
        role = role,
        serverSecret = config.secret,
        expirationMinutes = config.expirationMinutes
      )
    } yield Token(token, role)
  }

  def validateToken(token: String): ZIO[AppEnv, TarotError, TokenPayload] = {
    for {
      _ <- ZIO.logDebug(s"Attempting to validate token")

      config <- ZIO.serviceWith[AppEnv](_.appConfig.jwt)
      tokenPayload <- JwtService.validateToken(token, config.secret)
    } yield tokenPayload
  }

  private def getUser(userId: UserId): ZIO[AppEnv, TarotError, User] =
    for {
      userRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository.userRepository)
      user <- userRepository.getUser(userId).flatMap {
        case Some(user) =>
          ZIO.succeed(user)
        case None =>
          ZIO.logWarning(s"Authorization failed: user $userId not found") *>
            ZIO.fail(TarotError.Unauthorized(s"User $userId not found"))
      }
    } yield user

  private def getUserRole(userId: UserId, projectId: Option[ProjectId]): ZIO[AppEnv, TarotError, Role] =
    projectId match {
      case None =>
        ZIO.succeed(Role.PreProject)
      case Some(projectId) =>
        for {
          userAccessRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository.userProjectRepository)
          userProject <- userAccessRepository.getUserRole(userId, projectId)
          role <- userProject match {
            case Some(userProject) =>
              ZIO.succeed(userProject.role)
            case None =>
              ZIO.logWarning(s"Authorization failed: user $userId not found in project $projectId") *>
                ZIO.fail(TarotError.Unauthorized(s"User $userId not found in project $projectId"))
          }
        } yield role
    }
}