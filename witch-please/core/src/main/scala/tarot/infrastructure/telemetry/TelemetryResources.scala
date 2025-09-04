package tarot.infrastructure.telemetry

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.semconv.ServiceAttributes
import tarot.application.configurations.{TarotConfig, TelemetryConfig}
import zio.ZIO

object TelemetryResources {
  final val telemetryAppName = "arbitration-app"

  val telemetryResource: Resource = Resource.create(
    Attributes.of(ServiceAttributes.SERVICE_NAME, telemetryAppName))

  def getTelemetryConfig: ZIO[TarotConfig, Throwable, TelemetryConfig] =
    for {
      config <- ZIO.service[TarotConfig]
      telemetryConfig <- ZIO.fromOption(config.telemetry)
        .orElseFail(new RuntimeException("Telemetry config is missing"))
    } yield telemetryConfig
}
