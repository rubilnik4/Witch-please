package tarot.application.handlers

import tarot.application.handlers.users.*
import tarot.application.handlers.spreads.*
import zio.{ULayer, ZLayer}

object TarotCommandHandlerLayer {
  private val userCreateCommandHandlerLive: ULayer[UserCreateCommandHandler] =
    ZLayer.succeed(new UserCreateCommandHandlerLive)

  private val spreadCreateCommandHandlerLive: ULayer[SpreadCreateCommandHandler] =
    ZLayer.succeed(new SpreadCreateCommandHandlerLive)

  private val spreadPublishCommandHandlerLive: ULayer[SpreadPublishCommandHandler] =
    ZLayer.succeed(new SpreadPublishCommandHandlerLive)
    
  private val cardCommandHandlerLive: ULayer[CardCreateCommandHandler] =
    ZLayer.succeed(new CardCreateCommandHandlerLive)

  val tarotCommandHandlerLive: ULayer[TarotCommandHandlerLive] =
    (userCreateCommandHandlerLive ++
     spreadCreateCommandHandlerLive ++ spreadPublishCommandHandlerLive ++ cardCommandHandlerLive) >>>
      ZLayer.fromFunction {
        (userCreate:UserCreateCommandHandler,
          createSpread: SpreadCreateCommandHandler, publishSpread: SpreadPublishCommandHandler,
          createCard: CardCreateCommandHandler) =>
          TarotCommandHandlerLive(userCreate, createSpread, publishSpread, createCard)
  }
}
