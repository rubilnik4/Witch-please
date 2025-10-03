package tarot.application.queries

import tarot.application.queries.projects.ProjectsQueryHandler
import tarot.application.queries.users.UserByClientIdQueryHandler

final case class TarotQueryHandlerLive(
  userByClientIdQueryHandler: UserByClientIdQueryHandler,
  projectsQueryHandler: ProjectsQueryHandler
) extends TarotQueryHandler
