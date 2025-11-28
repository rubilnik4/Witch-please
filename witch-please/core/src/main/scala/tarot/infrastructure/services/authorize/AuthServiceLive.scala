package tarot.infrastructure.services.authorize

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.{Token, User, UserId, UserProject, UserRole}
import tarot.layers.TarotEnv
import zio.{Cause, ZIO}
import com.github.roundrop.bcrypt.*
import shared.models.tarot.authorize.{ClientType, Role}
import tarot.api.dto.tarot.authorize.TokenPayload
import tarot.application.commands.users.commands.CreateAuthorCommand
import tarot.domain.models.projects.ProjectId
import tarot.infrastructure.repositories.users.*

final case class AuthServiceLive(
  userRepository: UserRepository,
  userProjectRepository: UserProjectRepository
) extends AuthService {
  def issueToken(clientType: ClientType, userId: UserId, clientSecret: String)
      : ZIO[TarotEnv, TarotError, Token] = {
    for {
      _ <- ZIO.logDebug(s"Attempting to get auth token for user $userId")
      
      user <- getUser(userId)
      isValid <- ZIO.fromTry(clientSecret.isBcrypted(user.secretHash))
        .tapError(err => ZIO.logErrorCause(s"Decryption error for user $userId", Cause.fail(err)))
        .mapError(err => TarotError.Unauthorized(s"Decryption error for user $userId"))

      _ <- ZIO.unless(isValid)(
        ZIO.logWarning(s"Unauthorized token request: invalid secret for userId=$userId") *>
          ZIO.fail(TarotError.Unauthorized(s"Invalid client secret for user $userId"))
      )

      role <- getUserRole(userId)
      config <- ZIO.serviceWith[TarotEnv](_.config.jwt)
      token <- JwtService.generateToken(
        clientType = clientType,
        userId = userId.id,
        role = role,
        serverSecret = config.secret,
        expirationMinutes = config.expirationMinutes
      )
    } yield Token(token, role)
  }

  def validateToken(token: String): ZIO[TarotEnv, TarotError, TokenPayload] = {
    for {
      _ <- ZIO.logDebug(s"Attempting to validate token")

      config <- ZIO.serviceWith[TarotEnv](_.config.jwt)
      tokenPayload <- JwtService.validateToken(token, config.secret)
    } yield tokenPayload
  }

  private def getUser(userId: UserId): ZIO[TarotEnv, TarotError, User] =
    for {      
      user <- userRepository.getUser(userId).flatMap {
        case Some(user) =>
          ZIO.succeed(user)
        case None =>
          ZIO.logWarning(s"Authorization failed: user $userId not found") *>
            ZIO.fail(TarotError.Unauthorized(s"User $userId not found"))
      }
    } yield user

  private def getUserRole(userId: UserId): ZIO[TarotEnv, TarotError, Role] =
    for {
      projectIds <- userProjectRepository.getProjectIds(userId)
      projectId <- ZIO.fromOption(projectIds.headOption)
        .orElseFail(TarotError.NotFound(s"No project found for user $userId"))

      userProject <- userProjectRepository.getUserRole(userId, projectId)
      role <- userProject match {
        case Some(userProject) =>
          ZIO.succeed(userProject.role)
        case None =>
          ZIO.logWarning(s"Authorization failed: user $userId not found in project $projectId") *>
            ZIO.fail(TarotError.Unauthorized(s"User $userId not found in project $projectId"))
      }
    } yield role
}