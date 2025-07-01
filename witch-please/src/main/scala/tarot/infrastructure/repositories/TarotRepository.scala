package tarot.infrastructure.repositories

import tarot.domain.models.TarotError
import tarot.domain.models.contracts.{CardId, SpreadId}
import tarot.domain.models.spreads.{Card, Spread}
import zio.ZIO

trait TarotRepository {
  def getSpread(spreadId: SpreadId): ZIO[Any, TarotError, Option[Spread]]  
  def existsSpread(spreadId: SpreadId): ZIO[Any, TarotError, Boolean]
  def createSpread(spread: Spread): ZIO[Any, TarotError, SpreadId]
  
  def createCard(card: Card): ZIO[Any, TarotError, CardId]
}
