package tarot.application.handlers

import zio.{ULayer, ZLayer}

object TarotCommandHandlerLayer {
  private val spreadCommandHandlerLive: ULayer[SpreadCommandHandler] =
    ZLayer.succeed(new SpreadCommandHandlerLive)

  private val cardCommandHandlerLive: ULayer[CardCommandHandler] =
    ZLayer.succeed(new CardCommandHandlerLive)

  val tarotCommandHandlerLive: ULayer[TarotCommandHandlerLive] =
    (spreadCommandHandlerLive ++ cardCommandHandlerLive) >>> ZLayer.fromFunction {
    (spread: SpreadCommandHandler, card: CardCommandHandler) =>
      TarotCommandHandlerLive(spread, card)
  }
}
