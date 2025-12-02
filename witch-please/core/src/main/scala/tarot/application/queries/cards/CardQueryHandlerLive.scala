package tarot.application.queries.cards

import tarot.domain.models.TarotError
import tarot.domain.models.cards.Card
import tarot.domain.models.spreads.SpreadId
import tarot.infrastructure.repositories.cards.CardRepository
import tarot.layers.TarotEnv
import zio.ZIO

final class CardQueryHandlerLive(cardRepository: CardRepository) extends CardQueryHandler {
  def getCardsCount(spreadId: SpreadId): ZIO[TarotEnv, TarotError, Int] =
    for {
      _ <- ZIO.logInfo(s"Executing cards query by spreadId $spreadId")
      
      cardsCount <- cardRepository.getCardsCount(spreadId)
    } yield cardsCount.toInt

  def getCards(spreadId: SpreadId): ZIO[TarotEnv, TarotError, List[Card]] =
    for {
      _ <- ZIO.logInfo(s"Executing cards count query by spreadId $spreadId")
      
      cards <- cardRepository.getCards(spreadId)
    } yield cards  
}