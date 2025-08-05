package tarot.application.configurations

import zio.{Config, Duration}
import zio.config.magnolia.deriveConfig

final case class ProjectConfig(
  minFutureTime: Duration,
  maxFutureTime: Duration                            
)

object ProjectConfig {
  implicit val config: Config[ProjectConfig] = deriveConfig[ProjectConfig]
}
