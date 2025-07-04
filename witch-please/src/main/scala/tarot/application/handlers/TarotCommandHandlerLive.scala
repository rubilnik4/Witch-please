package tarot.application.handlers

final case class TarotCommandHandlerLive(
                                          spreadCommandHandler: SpreadCreateCommandHandler,
                                          cardCommandHandler: CardCreateCommandHandler
) extends TarotCommandHandler 
