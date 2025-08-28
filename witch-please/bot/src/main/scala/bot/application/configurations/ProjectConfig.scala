package bot.application.configurations

import zio.config.magnolia.deriveConfig
import zio.{Config, Duration}

final case class ProjectConfig(
  tarotUrl: String,                            
  minFutureTime: Duration,
  maxFutureTime: Duration                            
)

object ProjectConfig {
  implicit val config: Config[ProjectConfig] = deriveConfig[ProjectConfig]
}
