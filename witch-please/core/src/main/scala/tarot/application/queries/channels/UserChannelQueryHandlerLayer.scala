package tarot.application.queries.channels

import tarot.infrastructure.repositories.channels.UserChannelRepository
import tarot.infrastructure.repositories.users.{UserProjectRepository, UserRepository}
import zio.ZLayer

object UserChannelQueryHandlerLayer {
  val live: ZLayer[UserChannelRepository, Nothing, UserChannelQueryHandler] =
    ZLayer.fromFunction(new UserChannelQueryHandlerLive(_))
}