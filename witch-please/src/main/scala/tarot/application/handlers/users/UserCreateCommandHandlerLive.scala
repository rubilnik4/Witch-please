package tarot.application.handlers.users

import tarot.application.commands.users.UserCreateCommand
import tarot.domain.models.TarotError
import tarot.domain.models.auth.UserId
import tarot.layers.AppEnv
import zio.ZIO

final class UserCreateCommandHandlerLive extends UserCreateCommandHandler {
  def handle(command: UserCreateCommand): ZIO[AppEnv, TarotError, UserId] = {
    for {
      _ <- ZIO.logInfo(s"Executing create user command for ${command.externalUser}")

      userService <- ZIO.serviceWith[AppEnv](_.tarotService.userService)
      userId <- userService.createUser(command.externalUser)

      _ <- ZIO.logInfo(s"Successfully user created: $userId")
    } yield userId
  }
}
