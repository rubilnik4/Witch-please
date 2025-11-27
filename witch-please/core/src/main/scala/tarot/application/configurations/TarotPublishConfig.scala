package tarot.application.configurations

import zio.{Config, Duration}
import zio.config.magnolia.deriveConfig

final case class TarotPublishConfig(
  tick: Duration,
  lookAhead: Duration,
  batchLimit: Int,
  hardPastTime: Duration,
  maxFutureTime: Duration,
  maxCardOfDayDelay: Duration
)

object TarotPublishConfig {
  implicit val config: Config[TarotPublishConfig] = deriveConfig[TarotPublishConfig]
}
