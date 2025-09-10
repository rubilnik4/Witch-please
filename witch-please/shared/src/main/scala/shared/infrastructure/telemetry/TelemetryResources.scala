package shared.infrastructure.telemetry

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.semconv.ServiceAttributes
import shared.application.configurations.*
import zio.ZIO

object TelemetryResources {
  def telemetryResource(appName: String): Resource = Resource.create(
    Attributes.of(ServiceAttributes.SERVICE_NAME, appName))
}
