package tarot.application.commands

import tarot.application.commands.cardOfDay.CardOfDayCommandHandler
import tarot.application.commands.cards.CardCommandHandler
import tarot.application.commands.photos.PhotoCommandHandler
import tarot.application.commands.projects.*
import tarot.application.commands.spreads.*
import tarot.application.commands.users.*

trait TarotCommandHandler {
  def userCommandHandler: UserCommandHandler
  def projectCommandHandler: ProjectCommandHandler
  def spreadCommandHandler: SpreadCommandHandler
  def cardCommandHandler: CardCommandHandler
  def cardOfDayCommandHandler: CardOfDayCommandHandler
  def photoCommandHandler: PhotoCommandHandler
}