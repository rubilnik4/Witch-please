package tarot.application.commands.cardOfDay

import tarot.application.queries.cards.CardQueryHandler
import tarot.infrastructure.repositories.cardOfDay.CardOfDayRepository
import tarot.infrastructure.repositories.cards.CardRepository
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.repositories.users.{UserProjectRepository, UserRepository}
import zio.ZLayer

object CardOfDayCommandHandlerLayer {
  val live: ZLayer[CardOfDayRepository, Nothing, CardOfDayCommandHandler] =
    ZLayer.fromFunction(new CardOfDayCommandHandlerLive(_))
}
