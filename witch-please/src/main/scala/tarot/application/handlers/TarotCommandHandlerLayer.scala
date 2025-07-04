package tarot.application.handlers

import zio.{ULayer, ZLayer}

object TarotCommandHandlerLayer {
  private val spreadCommandHandlerLive: ULayer[SpreadCreateCommandHandler] =
    ZLayer.succeed(new SpreadCreateCommandHandlerLive)

  private val cardCommandHandlerLive: ULayer[CardCreateCommandHandler] =
    ZLayer.succeed(new CardCreateCommandHandlerLive)

  val tarotCommandHandlerLive: ULayer[TarotCommandHandlerLive] =
    (spreadCommandHandlerLive ++ cardCommandHandlerLive) >>> ZLayer.fromFunction {
    (spread: SpreadCreateCommandHandler, card: CardCreateCommandHandler) =>
      TarotCommandHandlerLive(spread, card)
  }
}
