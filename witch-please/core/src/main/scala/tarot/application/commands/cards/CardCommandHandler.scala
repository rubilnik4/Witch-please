package tarot.application.commands.cards

import tarot.application.commands.cards.commands.CreateCardCommand
import tarot.domain.models.TarotError
import tarot.domain.models.cards.CardId
import tarot.layers.TarotEnv
import zio.ZIO

trait CardCommandHandler {
  def createCard (command: CreateCardCommand): ZIO[TarotEnv, TarotError, CardId]
}