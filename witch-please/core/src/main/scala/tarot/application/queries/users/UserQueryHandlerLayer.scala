package tarot.application.queries.users

import tarot.infrastructure.repositories.users.{UserProjectRepository, UserRepository}
import zio.ZLayer

object UserQueryHandlerLayer {
  val live: ZLayer[UserRepository & UserProjectRepository, Nothing, UserQueryHandler] =
    ZLayer.fromFunction(new UserQueryHandlerLive(_,_))
}