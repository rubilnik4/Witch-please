package tarot.application.handlers

import tarot.application.handlers.projects.ProjectCreateCommandHandler
import tarot.application.handlers.users.UserCreateCommandHandler
import tarot.application.handlers.spreads.*

trait TarotCommandHandler {
  def userCreateCommandHandler: UserCreateCommandHandler
  def projectCreateCommandHandler: ProjectCreateCommandHandler
  def spreadCreateCommandHandler: SpreadCreateCommandHandler
  def spreadPublishCommandHandler: SpreadPublishCommandHandler
  def cardCreateCommandHandler: CardCreateCommandHandler
}