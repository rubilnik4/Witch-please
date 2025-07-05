package tarot.application.handlers

final case class TarotCommandHandlerLive(
  spreadCreateCommandHandler: SpreadCreateCommandHandler,
  spreadPublishCommandHandler: SpreadPublishCommandHandler,                                     
  cardCreateCommandHandler: CardCreateCommandHandler
) extends TarotCommandHandler 
