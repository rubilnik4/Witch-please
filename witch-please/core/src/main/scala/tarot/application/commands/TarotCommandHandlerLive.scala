package tarot.application.commands

import tarot.application.commands.projects.*
import tarot.application.commands.spreads.*
import tarot.application.commands.users.*

final case class TarotCommandHandlerLive(
  userCreateCommandHandler: UserCreateCommandHandler,
  projectCreateCommandHandler: ProjectCreateCommandHandler,                                     
  spreadCreateCommandHandler: SpreadCreateCommandHandler,
  spreadPublishCommandHandler: SpreadPublishCommandHandler,                                     
  cardCreateCommandHandler: CardCreateCommandHandler
) extends TarotCommandHandler 
