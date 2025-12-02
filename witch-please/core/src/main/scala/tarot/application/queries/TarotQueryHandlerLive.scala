package tarot.application.queries

import tarot.application.queries.cards.CardQueryHandler
import tarot.application.queries.photos.PhotoQueryHandler
import tarot.application.queries.projects.ProjectQueryHandler
import tarot.application.queries.spreads.SpreadQueryHandler
import tarot.application.queries.users.UserQueryHandler

final case class TarotQueryHandlerLive(
  userQueryHandler: UserQueryHandler,
  projectQueryHandler: ProjectQueryHandler,
  spreadQueryHandler: SpreadQueryHandler,
  cardQueryHandler: CardQueryHandler,
  photoQueryHandler: PhotoQueryHandler                                  
) extends TarotQueryHandler
