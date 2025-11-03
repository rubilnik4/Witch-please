package tarot.application.commands

import tarot.application.commands.projects.*
import tarot.application.commands.spreads.*
import tarot.application.commands.users.*
import zio.{ULayer, ZLayer}

object TarotCommandHandlerLayer {
  val tarotCommandHandlerLive: ULayer[TarotCommandHandlerLive] =
    (
      ZLayer.succeed(new UserCommandHandlerLive) ++
      ZLayer.succeed(new ProjectCommandHandlerLive) ++
      ZLayer.succeed(new SpreadCommandHandlerLive) ++
      ZLayer.succeed(new CardCommandHandlerLive)
    ) >>> ZLayer.fromFunction(TarotCommandHandlerLive.apply)
}
