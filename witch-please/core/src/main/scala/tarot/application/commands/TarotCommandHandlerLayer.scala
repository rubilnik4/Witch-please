package tarot.application.commands

import tarot.application.commands.cardOfDay.CardOfDayCommandHandlerLayer
import tarot.application.commands.cards.CardCommandHandlerLayer
import tarot.application.commands.photos.PhotoCommandHandlerLayer
import tarot.application.commands.projects.*
import tarot.application.commands.spreads.*
import tarot.application.commands.users.*
import tarot.infrastructure.repositories.TarotRepositoryLayer
import tarot.infrastructure.repositories.TarotRepositoryLayer.Repositories
import zio.ZLayer

object TarotCommandHandlerLayer {
  val live: ZLayer[Repositories, Throwable, TarotCommandHandler] =
    (
      UserCommandHandlerLayer.live ++
      ProjectCommandHandlerLayer.live ++
      SpreadCommandHandlerLayer.live ++
      CardCommandHandlerLayer.live ++
      CardOfDayCommandHandlerLayer.live ++
      PhotoCommandHandlerLayer.live
    ) >>> ZLayer.fromFunction(TarotCommandHandlerLive.apply)
}
