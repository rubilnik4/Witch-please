package tarot.application.commands

import tarot.application.commands.projects.*
import tarot.application.commands.spreads.*
import tarot.application.commands.users.*

trait TarotCommandHandler {
  def userCommandHandler: UserCreateCommandHandler
  def projectCommandHandler: ProjectCommandHandler
  def spreadCommandHandler: SpreadCommandHandler
  def cardCommandHandler: CardCommandHandler
}