package tarot.application.handlers

import zio.{ULayer, URLayer, ZLayer}

object TarotCommandHandlerLayer {
  private val spreadCommandHandlerLive: ULayer[SpreadCommandHandler] =
    ZLayer.succeed(new SpreadCommandHandlerLive)

  val tarotCommandHandlerLive: ULayer[TarotCommandHandlerLive] =
    (spreadCommandHandlerLive) >>> ZLayer.fromFunction {
    (spread: SpreadCommandHandler) =>
      TarotCommandHandlerLive(spread)
  }
}
