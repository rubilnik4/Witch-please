package shared.infrastructure.telemetry.tracing

import zio.telemetry.opentelemetry.tracing.Tracing

final case class TelemetryTracingLive(tracing: Tracing) 
  extends TelemetryTracing 