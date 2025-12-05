package tarot.application.commands.cards

import tarot.application.commands.cards.commands.*
import tarot.domain.models.TarotError
import tarot.domain.models.cards.{Card, CardId}
import tarot.domain.models.spreads.SpreadId
import tarot.layers.TarotEnv
import zio.ZIO

trait CardCommandHandler {
  def createCard (command: CreateCardCommand): ZIO[TarotEnv, TarotError, CardId]
  def updateCard(command: UpdateCardCommand): ZIO[TarotEnv, TarotError, Unit]
  def deleteCard(cardId: CardId): ZIO[TarotEnv, TarotError, Unit]
  def deleteCard(card: Card): ZIO[TarotEnv, TarotError, Unit]
}