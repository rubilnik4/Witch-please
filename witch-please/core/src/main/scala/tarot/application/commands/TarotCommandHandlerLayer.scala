package tarot.application.commands

import tarot.application.commands.cards.CardCommandHandlerLayer
import tarot.application.commands.projects.*
import tarot.application.commands.spreads.*
import tarot.application.commands.users.*
import tarot.application.configurations.TarotConfig
import tarot.infrastructure.repositories.TarotRepositoryLayer
import tarot.infrastructure.repositories.TarotRepositoryLayer.Repositories
import zio.ZLayer

object TarotCommandHandlerLayer {
  val live: ZLayer[Repositories, Throwable, TarotCommandHandler] =
    (
      UserCommandHandlerLayer.live ++
      ProjectCommandHandlerLayer.live ++
      SpreadCommandHandlerLayer.live ++
      CardCommandHandlerLayer.live
    ) >>> ZLayer.fromFunction(TarotCommandHandlerLive.apply)
}
