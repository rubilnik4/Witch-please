package tarot.application.commands.projects

import tarot.application.queries.cards.CardQueryHandler
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.repositories.users.{UserProjectRepository, UserRepository}
import zio.ZLayer

object ProjectCommandHandlerLayer {
  val live: ZLayer[UserRepository & UserProjectRepository, Nothing, ProjectCommandHandler] =
    ZLayer.fromFunction(new ProjectCommandHandlerLive(_, _))
}
