package tarot.application.handlers

import tarot.application.handlers.users.UserCreateCommandHandler
import tarot.application.handlers.spreads.{CardCreateCommandHandler, SpreadCreateCommandHandler, SpreadPublishCommandHandler}

trait TarotCommandHandler {
  def userCreateCommandHandler: UserCreateCommandHandler
  
  def spreadCreateCommandHandler: SpreadCreateCommandHandler
  def spreadPublishCommandHandler: SpreadPublishCommandHandler
  def cardCreateCommandHandler: CardCreateCommandHandler
}