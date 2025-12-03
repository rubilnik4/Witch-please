package tarot.application.commands.cards

import tarot.application.queries.cards.CardQueryHandler
import tarot.infrastructure.repositories.cards.CardRepository
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.repositories.users.{UserProjectRepository, UserRepository}
import zio.ZLayer

object CardCommandHandlerLayer {
  val live: ZLayer[CardRepository, Nothing, CardCommandHandler] =
    ZLayer.fromFunction(new CardCommandHandlerLive(_))
}
