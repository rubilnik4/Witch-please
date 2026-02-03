package tarot.application.jobs

import tarot.application.jobs.publish.PublishJob
import tarot.layers.TarotEnv
import zio.ZIO

trait TarotJob {
  def publishJob: PublishJob
}