package shared.infrastructure.telemetry.tracing

import zio.ZLayer
import zio.telemetry.opentelemetry.tracing.Tracing

object TelemetryTracingLayer {
  val live: ZLayer[Tracing, Throwable, TelemetryTracing] =
    ZLayer.fromFunction { (tracing: Tracing) =>
      TelemetryTracingLive(tracing)
    }
}
