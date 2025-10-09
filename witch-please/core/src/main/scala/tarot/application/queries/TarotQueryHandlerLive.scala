package tarot.application.queries

import tarot.application.queries.cards.CardsQueryHandler
import tarot.application.queries.projects.ProjectsQueryHandler
import tarot.application.queries.spreads.SpreadsQueryHandler
import tarot.application.queries.users.UserByClientIdQueryHandler

final case class TarotQueryHandlerLive(
  userByClientIdQueryHandler: UserByClientIdQueryHandler,
  projectsQueryHandler: ProjectsQueryHandler,
  spreadsQueryHandler: SpreadsQueryHandler,
  cardsQueryHandler: CardsQueryHandler                                    
) extends TarotQueryHandler
