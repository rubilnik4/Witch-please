package tarot.application.commands.users

import tarot.application.queries.cards.CardQueryHandler
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.repositories.users.{UserProjectRepository, UserRepository}
import zio.ZLayer

object UserCommandHandlerLayer {
  val live: ZLayer[UserRepository, Nothing, UserCommandHandler] =
    ZLayer.fromFunction(new UserCommandHandlerLive(_))
}
