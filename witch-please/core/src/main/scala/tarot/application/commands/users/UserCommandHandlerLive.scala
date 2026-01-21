package tarot.application.commands.users

import tarot.application.commands.users.commands.CreateAuthorCommand
import tarot.domain.models.TarotError
import tarot.domain.models.authorize.*
import tarot.domain.models.projects.*
import tarot.domain.models.users.{User, UserId}
import tarot.infrastructure.repositories.users.*
import tarot.infrastructure.services.users.UserService
import tarot.layers.TarotEnv
import zio.ZIO

final class UserCommandHandlerLive(
  userRepository: UserRepository
) extends UserCommandHandler {
  override def createAuthor(command: CreateAuthorCommand): ZIO[TarotEnv, TarotError, UserId] =
    for {
      _ <- ZIO.logInfo(s"Executing create user ${command.name} command")
      
      exists <- userRepository.existsUserByClientId(command.clientId)
      _ <- ZIO.when(exists)(
        ZIO.logError(s"User ${command.clientId} already exists") *>
          ZIO.fail(TarotError.Conflict(s"User ${command.clientId} already exists"))
      )

      secretHash <- UserService.hashSecret(command.clientSecret)
      user <- User.toDomain(command, secretHash)
      userId <- userRepository.createUser(user)

      projectCommandHandler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.projectCommandHandler)
      _ <- projectCommandHandler.createDefaultProject(userId)
    } yield userId
}
