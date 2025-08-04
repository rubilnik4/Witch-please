package tarot.application.handlers

import tarot.application.handlers.projects.ProjectCreateCommandHandler
import tarot.application.handlers.users.UserCreateCommandHandler
import tarot.application.handlers.spreads.*

final case class TarotCommandHandlerLive(
  userCreateCommandHandler: UserCreateCommandHandler,
  projectCreateCommandHandler: ProjectCreateCommandHandler,                                     
  spreadCreateCommandHandler: SpreadCreateCommandHandler,
  spreadPublishCommandHandler: SpreadPublishCommandHandler,                                     
  cardCreateCommandHandler: CardCreateCommandHandler
) extends TarotCommandHandler 
