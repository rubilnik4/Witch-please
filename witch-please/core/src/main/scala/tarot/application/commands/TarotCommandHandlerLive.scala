package tarot.application.commands

import tarot.application.commands.cards.CardCommandHandler
import tarot.application.commands.projects.*
import tarot.application.commands.spreads.*
import tarot.application.commands.users.*

final case class TarotCommandHandlerLive(
  userCommandHandler: UserCommandHandler,
  projectCommandHandler: ProjectCommandHandler,
  spreadCommandHandler: SpreadCommandHandler,
  cardCommandHandler: CardCommandHandler
) extends TarotCommandHandler 
