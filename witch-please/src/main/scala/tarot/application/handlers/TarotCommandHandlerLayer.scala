package tarot.application.handlers

import zio.{ULayer, ZLayer}

object TarotCommandHandlerLayer {
  private val spreadCreateCommandHandlerLive: ULayer[SpreadCreateCommandHandler] =
    ZLayer.succeed(new SpreadCreateCommandHandlerLive)

  private val spreadPublishCommandHandlerLive: ULayer[SpreadPublishCommandHandler] =
    ZLayer.succeed(new SpreadPublishCommandHandlerLive)
    
  private val cardCommandHandlerLive: ULayer[CardCreateCommandHandler] =
    ZLayer.succeed(new CardCreateCommandHandlerLive)

  val tarotCommandHandlerLive: ULayer[TarotCommandHandlerLive] =
    (spreadCreateCommandHandlerLive ++ spreadPublishCommandHandlerLive ++ cardCommandHandlerLive) >>> 
      ZLayer.fromFunction {
        (createSpread: SpreadCreateCommandHandler, publishSpread: SpreadPublishCommandHandler,
         createCard: CardCreateCommandHandler) =>
          TarotCommandHandlerLive(createSpread, publishSpread, createCard)
  }
}
