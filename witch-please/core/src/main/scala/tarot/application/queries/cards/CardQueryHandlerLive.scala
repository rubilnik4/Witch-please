package tarot.application.queries.cards

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.User
import tarot.domain.models.cards.Card
import tarot.domain.models.projects.Project
import tarot.domain.models.spreads.{Spread, SpreadId}
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.layers.TarotEnv
import zio.ZIO

final class CardQueryHandlerLive(spreadRepository: SpreadRepository) extends CardQueryHandler {
  def getCardsCount(spreadId: SpreadId): ZIO[TarotEnv, TarotError, Int] =
    for {
      _ <- ZIO.logInfo(s"Executing cards query by spreadId $spreadId")
      
      cardsCount <- spreadRepository.getCardsCount(spreadId)
    } yield cardsCount.toInt

  def getCards(spreadId: SpreadId): ZIO[TarotEnv, TarotError, List[Card]] =
    for {
      _ <- ZIO.logInfo(s"Executing cards count query by spreadId $spreadId")
      
      cards <- spreadRepository.getCards(spreadId)
    } yield cards  
}