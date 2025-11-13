package tarot.application.jobs.spreads

import tarot.domain.models.TarotError
import tarot.layers.TarotEnv
import zio.ZIO

trait SpreadJob {
  def run: ZIO[TarotEnv, Nothing, Unit]
  def publishSpreads(): ZIO[TarotEnv, TarotError, List[SpreadPublishResult]]
}
