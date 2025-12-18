package tarot.application.commands.cardOfDay

import tarot.application.commands.cardOfDay.commands.CreateCardOfDayCommand
import tarot.application.commands.cards.commands.*
import tarot.domain.models.TarotError
import tarot.domain.models.cardOfDay.CardOfDayId
import tarot.domain.models.cards.{Card, CardId}
import tarot.domain.models.spreads.SpreadId
import tarot.layers.TarotEnv
import zio.ZIO

import java.time.Instant

trait CardOfDayCommandHandler {
  def createCardOfDay(command: CreateCardOfDayCommand): ZIO[TarotEnv, TarotError, CardOfDayId]
  def publishCardOfDay(cardOfDayId: CardOfDayId, publishAt: Instant): ZIO[TarotEnv, TarotError, Unit]
}