package tarot.application.configurations

import zio.{Config, Duration}
import zio.config.magnolia.deriveConfig

final case class ProjectConfig(
//    maxHistorySize: Int,
//    spreadThreshold: BigDecimal,
//    assetLoadingDelay: Duration,
//    assets: AssetConfig
)

object ProjectConfig {
  implicit val config: Config[ProjectConfig] = deriveConfig[ProjectConfig]
}
