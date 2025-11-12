package tarot.infrastructure.jobs.spreads

import tarot.layers.TarotEnv
import zio.ZIO

trait SpreadJob {
  def run: ZIO[TarotEnv, Nothing, Unit]
}
