package tarot.application.commands.spreads

import tarot.application.queries.cards.CardQueryHandler
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.repositories.users.{UserProjectRepository, UserRepository}
import zio.ZLayer

object SpreadCommandHandlerLayer {
  val live: ZLayer[SpreadRepository, Nothing, SpreadCommandHandler] =
    ZLayer.fromFunction(new SpreadCommandHandlerLive(_))
}
