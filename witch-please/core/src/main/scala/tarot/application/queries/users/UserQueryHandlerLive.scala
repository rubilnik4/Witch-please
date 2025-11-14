package tarot.application.queries.users

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.User
import tarot.infrastructure.repositories.users.UserRepository
import tarot.layers.TarotEnv
import zio.ZIO

final class UserQueryHandlerLive(userRepository: UserRepository) extends UserQueryHandler {
  def getUserByClientId(clientId: String): ZIO[TarotEnv, TarotError, User] =
    for {
      _ <- ZIO.logInfo(s"Executing user query by clientId $clientId")
      
      userOption <- userRepository.getUserByClientId(clientId)
      user <- ZIO.fromOption(userOption)
        .orElseFail(TarotError.NotFound(s"user by clientId ${clientId} not found"))
    } yield user
}