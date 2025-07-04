package tarot.application.handlers

trait TarotCommandHandler {
  def spreadCommandHandler: SpreadCreateCommandHandler
  def cardCommandHandler: CardCreateCommandHandler
}