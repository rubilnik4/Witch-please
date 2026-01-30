package tarot.application.commands.channels

import tarot.application.commands.channels.commands.{CreateUserChannelCommand, UpdateUserChannelCommand}
import tarot.domain.models.TarotError
import tarot.domain.models.channels.{UserChannel, UserChannelId, UserChannelUpdate}
import tarot.infrastructure.repositories.channels.UserChannelRepository
import tarot.layers.TarotEnv
import zio.ZIO

final class UserChannelCommandHandlerLive(
  userChannelRepository: UserChannelRepository                                       
) extends UserChannelCommandHandler {

  override def createUserChannel(command: CreateUserChannelCommand): ZIO[TarotEnv, TarotError, UserChannelId] =
    for {
      _ <- ZIO.logInfo(s"Executing create user channel ${command.channelId} command for user ${command.userId}")
      
      userChannelMaybe <- userChannelRepository.getUserChannelByUser(command.userId)
      _ <- ZIO.when(userChannelMaybe.nonEmpty)(
        ZIO.logError(s"Can't create user channel. User ${command.userId} already has channel") *>
          ZIO.fail(TarotError.Conflict(s"Can't create user channel. User ${command.userId} already has channel"))
      )
      
      userChannel <- UserChannel.toDomain(command)
      userChannelId <- userChannelRepository.createUserChannel(userChannel)
    } yield userChannelId

  override def updateUserChannel(command: UpdateUserChannelCommand): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Executing update channel ${command.channelId} command for user channel ${command.userChannelId}")    

      userChannel = UserChannelUpdate.toDomain(command)
      userChannelId <- userChannelRepository.updateUserChannel(command.userChannelId, userChannel)
    } yield ()    
}
