package tarot.application.queries.users

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.User
import tarot.layers.TarotEnv
import zio.ZIO

final class UserQueryHandlerLive extends UserQueryHandler {
  def getUserByClientId(clientId: String): ZIO[TarotEnv, TarotError, User] =
    for {
      _ <- ZIO.logInfo(s"Executing user query by clientId $clientId")

      repository <- ZIO.serviceWith[TarotEnv](_.tarotRepository.userRepository)
      userOption <- repository.getUserByClientId(clientId)
      user <- ZIO.fromOption(userOption)
        .orElseFail(TarotError.NotFound(s"user by clientId ${clientId} not found"))
    } yield user
}