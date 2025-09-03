package tarot.application.queries.users

import tarot.application.queries.QueryHandler
import tarot.domain.models.authorize.User

trait UserByClientIdQueryHandler extends QueryHandler[UserByClientIdQuery, User]
