package tarot.application.configurations

import zio.Config
import zio.config.magnolia.deriveConfig

final case class TelemetryConfig(otelEndpoint: String, prometheusPort: String, logLevel: String)

object TelemetryConfig {
  implicit val config: Config[TelemetryConfig] = deriveConfig[TelemetryConfig]
}
