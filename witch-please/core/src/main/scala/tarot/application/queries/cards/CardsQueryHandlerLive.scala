package tarot.application.queries.cards

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.User
import tarot.domain.models.cards.Card
import tarot.domain.models.projects.Project
import tarot.domain.models.spreads.Spread
import tarot.layers.TarotEnv
import zio.ZIO

final class CardsQueryHandlerLive extends CardsQueryHandler {
  def handle(query: CardsQuery): ZIO[TarotEnv, TarotError, List[Card]] =
    for {
      _ <- ZIO.logInfo(s"Executing cards query by spreadId ${query.spreadId}")

      repository <- ZIO.serviceWith[TarotEnv](_.tarotRepository.spreadRepository)
      cards <- repository.getCards(query.spreadId)
    } yield cards
}