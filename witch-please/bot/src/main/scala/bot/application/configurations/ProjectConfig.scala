package bot.application.configurations

import zio.config.magnolia.deriveConfig
import zio.{Config, Duration}

final case class ProjectConfig(
  minFutureTime: Duration,
  maxFutureTime: Duration                            
)

object ProjectConfig {
  implicit val config: Config[ProjectConfig] = deriveConfig[ProjectConfig]
}
