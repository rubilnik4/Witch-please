package tarot.application.queries.projects

import tarot.application.queries.QueryHandler
import tarot.domain.models.authorize.User
import tarot.domain.models.projects.Project

trait ProjectsQueryHandler extends QueryHandler[ProjectsQuery, List[Project]]
