package tarot.application.queries.channels

import tarot.domain.models.TarotError
import tarot.domain.models.channels.UserChannel
import tarot.domain.models.users.{Author, User, UserId}
import tarot.infrastructure.repositories.channels.UserChannelRepository
import tarot.infrastructure.repositories.users.*
import tarot.layers.TarotEnv
import zio.ZIO

final class UserChannelQueryHandlerLive(
  userChannelRepository: UserChannelRepository
) extends UserChannelQueryHandler {

  override def getDefaultUserChannel(userId: UserId): ZIO[TarotEnv, TarotError, UserChannel] =
    for {
      _ <- ZIO.logInfo(s"Executing default channel query by userId $userId")
      
      userChannelMaybe <- userChannelRepository.getUserChannel(userId)
      userChannel <- ZIO.fromOption(userChannelMaybe)
        .orElseFail(TarotError.NotFound(s"User channel by userId $userId not found"))
    } yield userChannel
}