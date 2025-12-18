package tarot.application.queries.spreads

import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.repositories.users.UserProjectRepository
import zio.ZLayer

object SpreadQueryHandlerLayer {
  val live: ZLayer[SpreadRepository & UserProjectRepository, Nothing, SpreadQueryHandler] =
    ZLayer.fromFunction(SpreadsQueryHandlerLive(_,_))
}
