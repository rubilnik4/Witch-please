package tarot.application.queries

import tarot.application.queries.users.*
import zio.{ULayer, ZLayer}

object TarotQueryHandlerLayer {
  val tarotQueryHandlerLive: ULayer[TarotQueryHandlerLive] =
    (
      ZLayer.succeed(new UserByClientIdQueryHandlerLive)
    ) >>> ZLayer.fromFunction(TarotQueryHandlerLive.apply)
}
