package tarot.infrastructure.services.users

import com.github.roundrop.bcrypt.*
import tarot.api.dto.tarot.auth.TokenPayload
import tarot.domain.models.TarotError
import tarot.domain.models.auth.*
import tarot.domain.models.projects.ProjectId
import tarot.layers.AppEnv
import zio.{Cause, ZIO}

final case class UserServiceLive() extends UserService {
  def createUser(externalUser: ExternalUser): ZIO[AppEnv, TarotError, UserId] =
    for {
      _ <- ZIO.logDebug(s"Attempting to create user $externalUser")

      secretHash <- ZIO.fromTry(externalUser.clientSecret.bcrypt(12))
        .tapError(err => ZIO.logErrorCause(s"Incryption error for user $externalUser", Cause.fail(err)))
        .mapError(err => TarotError.Unauthorized(s"Incryption error for user $externalUser"))
      user <- User.fromExternal(externalUser, secretHash)

      userRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository.userRepository)
      userId <- userRepository.createUser(user)
    } yield userId
}