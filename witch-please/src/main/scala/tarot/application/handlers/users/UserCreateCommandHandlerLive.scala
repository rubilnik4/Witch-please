package tarot.application.handlers.users

import tarot.application.commands.users.UserCreateCommand
import tarot.domain.models.TarotError
import tarot.domain.models.auth.{User, UserId}
import tarot.infrastructure.services.users.UserService
import tarot.layers.AppEnv
import zio.ZIO

final class UserCreateCommandHandlerLive extends UserCreateCommandHandler {
  def handle(command: UserCreateCommand): ZIO[AppEnv, TarotError, UserId] = {
    val externalUser = command.externalUser
    for {
      _ <- ZIO.logInfo(s"Executing create user command for ${externalUser}")

      userRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository.userRepository)

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
