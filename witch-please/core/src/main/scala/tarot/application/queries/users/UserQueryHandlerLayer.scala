package tarot.application.queries.users

import tarot.infrastructure.repositories.users.UserRepository
import zio.ZLayer

object UserQueryHandlerLayer {
  val live: ZLayer[UserRepository, Nothing, UserQueryHandler] =
    ZLayer.fromFunction(new UserQueryHandlerLive(_))
}