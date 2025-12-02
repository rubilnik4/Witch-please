package tarot.application.queries.cards

import tarot.domain.models.TarotError
import tarot.domain.models.cards.Card
import tarot.domain.models.spreads.SpreadId
import tarot.layers.TarotEnv
import zio.ZIO

trait CardQueryHandler {
  def getCardsCount(spreadId: SpreadId): ZIO[TarotEnv, TarotError, Int]
  def getCards(spreadId: SpreadId): ZIO[TarotEnv, TarotError, List[Card]]   
}
