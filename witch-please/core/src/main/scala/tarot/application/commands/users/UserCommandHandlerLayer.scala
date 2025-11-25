package tarot.application.commands.users

import tarot.infrastructure.repositories.users.{UserProjectRepository, UserRepository}
import zio.ZLayer

object UserCommandHandlerLayer {
  val live: ZLayer[UserRepository, Nothing, UserCommandHandler] =
    ZLayer.fromFunction(new UserCommandHandlerLive(_))
}
