package tarot.application.jobs.publish

import tarot.domain.models.TarotError
import tarot.layers.TarotEnv
import zio.ZIO

trait PublishJob {
  def run: ZIO[TarotEnv, Nothing, Unit]
  def publish(): ZIO[TarotEnv, TarotError, List[PublishJobResult]]
}
