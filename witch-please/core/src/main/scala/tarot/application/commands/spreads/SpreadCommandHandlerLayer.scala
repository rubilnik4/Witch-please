package tarot.application.commands.spreads

import tarot.application.queries.cards.CardQueryHandler
import tarot.infrastructure.repositories.cards.CardRepository
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.repositories.users.{UserProjectRepository, UserRepository}
import zio.ZLayer

object SpreadCommandHandlerLayer {
  val live: ZLayer[SpreadRepository & CardRepository, Nothing, SpreadCommandHandler] =
    ZLayer.fromFunction(new SpreadCommandHandlerLive(_, _))
}
