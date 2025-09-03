package tarot.application.commands

import tarot.application.commands.projects.*
import tarot.application.commands.spreads.*
import tarot.application.commands.users.*

trait TarotCommandHandler {
  def userCreateCommandHandler: UserCreateCommandHandler
  def projectCreateCommandHandler: ProjectCreateCommandHandler
  def spreadCreateCommandHandler: SpreadCreateCommandHandler
  def spreadPublishCommandHandler: SpreadPublishCommandHandler
  def cardCreateCommandHandler: CardCreateCommandHandler
}