package tarot.application.queries.spreads

import tarot.application.queries.QueryHandler
import tarot.domain.models.authorize.User
import tarot.domain.models.projects.Project
import tarot.domain.models.spreads.Spread

trait SpreadsQueryHandler extends QueryHandler[SpreadsQuery, List[Spread]]
