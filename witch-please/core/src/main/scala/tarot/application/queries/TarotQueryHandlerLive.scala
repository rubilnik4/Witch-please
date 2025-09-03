package tarot.application.queries

import tarot.application.queries.users.UserByClientIdQueryHandler

final case class TarotQueryHandlerLive(
  userByClientIdQueryHandler: UserByClientIdQueryHandler,
) extends TarotQueryHandler
