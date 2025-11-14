package tarot.application.commands.users

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.{ExternalUser, User, UserId}
import tarot.infrastructure.repositories.users.UserRepository
import tarot.infrastructure.services.users.UserService
import tarot.layers.TarotEnv
import zio.ZIO

final class UserCommandHandlerLive(userRepository: UserRepository) extends UserCommandHandler {
  def createUser(externalUser: ExternalUser): ZIO[TarotEnv, TarotError, UserId] = {   
    for {
      _ <- ZIO.logInfo(s"Executing create user command for $externalUser")
      
      exists <- userRepository.existsUserByClientId(externalUser.clientId)
      _ <- ZIO.when(exists)(
        ZIO.logError(s"User ${externalUser.clientId} already exists") *>
          ZIO.fail(TarotError.Conflict("User ${externalUser.clientId} already exists"))
      )

      secretHash <- UserService.hashSecret(externalUser.clientSecret)
      user <- User.toDomain(externalUser, secretHash)
      userId <- userRepository.createUser(user)

      _ <- ZIO.logInfo(s"Successfully user created: $userId")
    } yield userId
  }
}
