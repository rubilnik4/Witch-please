package tarot.application.handlers

trait TarotCommandHandler {
  def spreadCreateCommandHandler: SpreadCreateCommandHandler
  def spreadPublishCommandHandler: SpreadPublishCommandHandler
  def cardCreateCommandHandler: CardCreateCommandHandler
}