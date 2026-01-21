package tarot.application.commands.channels

import tarot.application.commands.channels.commands.CreateUserChannelCommand
import tarot.domain.models.TarotError
import tarot.domain.models.channels.{UserChannel, UserChannelId}
import tarot.infrastructure.repositories.channels.UserChannelRepository
import tarot.infrastructure.repositories.users.*
import tarot.layers.TarotEnv
import zio.ZIO

final class UserChannelCommandHandlerLive(
  userChannelRepository: UserChannelRepository                                       
) extends UserChannelCommandHandler {

  override def createUserChannel(command: CreateUserChannelCommand): ZIO[TarotEnv, TarotError, UserChannelId] =
    for {
      _ <- ZIO.logInfo(s"Executing create user channel ${command.chatId} command for user ${command.userId}")
      
      userChannelMaybe <- userChannelRepository.getUserChannel(command.userId)
      _ <- ZIO.when(userChannelMaybe.nonEmpty)(
        ZIO.logError(s"Can't create user channel. User ${command.userId} already has channel") *>
          ZIO.fail(TarotError.Conflict(s"Can't create user channel. User ${command.userId} already has channel"))
      )
      
      userChannel <- UserChannel.toDomain(command)
      userChannelId <- userChannelRepository.createUserChannel(userChannel)
    } yield userChannelId
}
