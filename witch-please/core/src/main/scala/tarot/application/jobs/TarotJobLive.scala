package tarot.application.jobs

import tarot.application.jobs.publish.PublishJob
import tarot.layers.TarotEnv
import zio.{ZIO, ZLayer}

final case class TarotJobLive(
  publishJob: PublishJob
) extends TarotJob 
