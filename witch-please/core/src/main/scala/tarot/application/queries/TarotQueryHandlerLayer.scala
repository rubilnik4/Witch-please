package tarot.application.queries

import tarot.application.queries.projects.ProjectsQueryHandlerLive
import tarot.application.queries.users.*
import zio.{ULayer, ZLayer}

object TarotQueryHandlerLayer {
  val tarotQueryHandlerLive: ULayer[TarotQueryHandlerLive] =
    (
      ZLayer.succeed(new UserByClientIdQueryHandlerLive) ++
      ZLayer.succeed(new ProjectsQueryHandlerLive)
    ) >>> ZLayer.fromFunction(TarotQueryHandlerLive.apply)
}
