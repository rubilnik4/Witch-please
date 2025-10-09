package tarot.application.queries.cards

import tarot.application.queries.QueryHandler
import tarot.domain.models.authorize.User
import tarot.domain.models.cards.Card
import tarot.domain.models.projects.Project
import tarot.domain.models.spreads.Spread

trait CardsQueryHandler extends QueryHandler[CardsQuery, List[Card]]
