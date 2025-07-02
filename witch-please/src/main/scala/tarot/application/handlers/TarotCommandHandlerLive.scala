package tarot.application.handlers

final case class TarotCommandHandlerLive(
  spreadCommandHandler: SpreadCommandHandler,
  cardCommandHandler: CardCommandHandler
) extends TarotCommandHandler 
