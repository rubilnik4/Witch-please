package tarot.application.queries

import tarot.application.configurations.TarotConfig
import tarot.application.queries.cards.*
import tarot.application.queries.projects.*
import tarot.application.queries.spreads.*
import tarot.application.queries.users.*
import tarot.infrastructure.repositories.TarotRepositoryLayer
import tarot.infrastructure.repositories.TarotRepositoryLayer.Repositories
import zio.ZLayer

object TarotQueryHandlerLayer {
  val live: ZLayer[Repositories, Throwable, TarotQueryHandler] =
    (
      UserQueryHandlerLayer.live ++
      ProjectQueryHandlerLayer.live ++
      SpreadQueryHandlerLayer.live ++
      CardQueryHandlerLayer.live
    ) >>> ZLayer.fromFunction(TarotQueryHandlerLive.apply)
}
