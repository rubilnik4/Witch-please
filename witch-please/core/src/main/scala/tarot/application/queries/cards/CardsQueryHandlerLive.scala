package tarot.application.queries.cards

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.User
import tarot.domain.models.cards.Card
import tarot.domain.models.projects.Project
import tarot.domain.models.spreads.{Spread, SpreadId}
import tarot.layers.TarotEnv
import zio.ZIO

final class CardsQueryHandlerLive extends CardsQueryHandler {
  def getCardsCount(spreadId: SpreadId): ZIO[TarotEnv, TarotError, Int] =
    for {
      _ <- ZIO.logInfo(s"Executing cards query by spreadId $spreadId")

      repository <- ZIO.serviceWith[TarotEnv](_.tarotRepository.spreadRepository)
      cardsCount <- repository.getCardsCount(spreadId)
    } yield cardsCount.toInt

  def getCards(spreadId: SpreadId): ZIO[TarotEnv, TarotError, List[Card]] =
    for {
      _ <- ZIO.logInfo(s"Executing cards count query by spreadId $spreadId")

      repository <- ZIO.serviceWith[TarotEnv](_.tarotRepository.spreadRepository)
      cards <- repository.getCards(spreadId)
    } yield cards  
}