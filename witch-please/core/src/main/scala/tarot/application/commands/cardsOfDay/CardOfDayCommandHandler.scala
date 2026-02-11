package tarot.application.commands.cardsOfDay

import tarot.application.commands.cardsOfDay.commands.*
import tarot.application.commands.cards.commands.*
import tarot.domain.models.TarotError
import tarot.domain.models.cardsOfDay.{CardOfDay, CardOfDayId}
import tarot.domain.models.cards.{Card, CardId}
import tarot.domain.models.spreads.SpreadId
import tarot.layers.TarotEnv
import zio.ZIO

import java.time.Instant

trait CardOfDayCommandHandler {
  def createCardOfDay(command: CreateCardOfDayCommand): ZIO[TarotEnv, TarotError, CardOfDayId]
  def updateCardOfDay(command: UpdateCardOfDayCommand): ZIO[TarotEnv, TarotError, Unit]
  def deleteCardOfDay(cardOfDayId: CardOfDayId): ZIO[TarotEnv, TarotError, Unit]
  def publishCardOfDay(cardOfDayId: CardOfDayId, publishAt: Instant): ZIO[TarotEnv, TarotError, Unit]
  def cloneCardOfDay(spreadId: SpreadId, cloneSpreadId: SpreadId): ZIO[TarotEnv, TarotError, CardOfDayId]
}