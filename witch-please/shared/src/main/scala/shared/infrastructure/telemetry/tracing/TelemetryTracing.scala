package shared.infrastructure.telemetry.tracing

import zio.telemetry.opentelemetry.tracing.Tracing

trait TelemetryTracing {
  def tracing: Tracing
}
