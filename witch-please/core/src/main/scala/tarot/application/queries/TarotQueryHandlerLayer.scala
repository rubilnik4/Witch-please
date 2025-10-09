package tarot.application.queries

import tarot.application.queries.cards.{CardsQueryHandler, CardsQueryHandlerLive}
import tarot.application.queries.projects.ProjectsQueryHandlerLive
import tarot.application.queries.spreads.{SpreadsQueryHandler, SpreadsQueryHandlerLive}
import tarot.application.queries.users.*
import zio.{ULayer, ZLayer}

object TarotQueryHandlerLayer {
  val tarotQueryHandlerLive: ULayer[TarotQueryHandlerLive] =
    (
      ZLayer.succeed(new UserByClientIdQueryHandlerLive) ++
      ZLayer.succeed(new ProjectsQueryHandlerLive) ++
      ZLayer.succeed(new SpreadsQueryHandlerLive) ++
      ZLayer.succeed(new CardsQueryHandlerLive)
    ) >>> ZLayer.fromFunction(TarotQueryHandlerLive.apply)
}
