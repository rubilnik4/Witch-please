package tarot.application.queries

import tarot.application.queries.cards.CardsQueryHandler
import tarot.application.queries.projects.ProjectsQueryHandler
import tarot.application.queries.spreads.SpreadsQueryHandler
import tarot.application.queries.users.UserQueryHandler

final case class TarotQueryHandlerLive(
                                        userQueryHandler: UserQueryHandler,
                                        projectsQueryHandler: ProjectsQueryHandler,
                                        spreadsQueryHandler: SpreadsQueryHandler,
                                        cardsQueryHandler: CardsQueryHandler                                    
) extends TarotQueryHandler
