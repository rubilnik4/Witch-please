package tarot.application.queries.users

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.User
import tarot.layers.AppEnv
import zio.ZIO

final class UserByClientIdQueryHandlerLive extends UserByClientIdQueryHandler {
  def handle(query: UserByClientIdQuery): ZIO[AppEnv, TarotError, User] =
    for {
      _ <- ZIO.logInfo(s"Executing user query by clientId ${query.clientId}")

      repository <- ZIO.serviceWith[AppEnv](_.tarotRepository.userRepository)
      userOption <- repository.getUserByClientId(query.clientId)
      user <- ZIO.fromOption(userOption)
        .orElseFail(TarotError.NotFound(s"user by clientId ${query.clientId} not found"))
    } yield user
}