package tarot.application.commands.channels

import tarot.infrastructure.repositories.channels.UserChannelRepository
import zio.ZLayer

object UserChannelCommandHandlerLayer {
  val live: ZLayer[UserChannelRepository, Nothing, UserChannelCommandHandler] =
    ZLayer.fromFunction(new UserChannelCommandHandlerLive(_))
}
