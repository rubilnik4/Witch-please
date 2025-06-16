package tarot.infrastructure.repositories

import tarot.domain.models.TarotError
import tarot.domain.models.contracts.SpreadId
import tarot.domain.models.spreads.Spread
import zio.ZIO

trait TarotRepository {
  def createSpread(spread: Spread): ZIO[Any, TarotError, SpreadId]
}
