package tarot.application.handlers

import tarot.application.handlers.projects.*
import tarot.application.handlers.users.*
import tarot.application.handlers.spreads.*
import zio.{ULayer, ZLayer}

object TarotCommandHandlerLayer {
  val tarotCommandHandlerLive: ULayer[TarotCommandHandlerLive] =
    (
      ZLayer.succeed(new UserCreateCommandHandlerLive) ++
      ZLayer.succeed(new ProjectCreateCommandHandlerLive) ++
      ZLayer.succeed(new SpreadCreateCommandHandlerLive) ++
      ZLayer.succeed(new SpreadPublishCommandHandlerLive) ++
      ZLayer.succeed(new CardCreateCommandHandlerLive)
    ) >>> ZLayer.fromFunction(TarotCommandHandlerLive.apply)
}
