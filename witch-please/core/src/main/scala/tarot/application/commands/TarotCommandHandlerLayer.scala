package tarot.application.commands

import tarot.application.commands.projects.*
import tarot.application.commands.spreads.*
import tarot.application.commands.users.*
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
