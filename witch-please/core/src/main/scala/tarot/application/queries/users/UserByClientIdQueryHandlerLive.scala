package tarot.application.queries.users

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.User
import tarot.layers.TarotEnv
import zio.ZIO

final class UserByClientIdQueryHandlerLive extends UserByClientIdQueryHandler {
  def handle(query: UserByClientIdQuery): ZIO[TarotEnv, TarotError, User] =
    for {
      _ <- ZIO.logInfo(s"Executing user query by clientId ${query.clientId}")

      repository <- ZIO.serviceWith[TarotEnv](_.tarotRepository.userRepository)
      userOption <- repository.getUserByClientId(query.clientId)
      user <- ZIO.fromOption(userOption)
        .orElseFail(TarotError.NotFound(s"user by clientId ${query.clientId} not found"))
    } yield user
}