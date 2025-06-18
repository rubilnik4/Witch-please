package tarot.application.telemetry.tracing

import io.opentelemetry.context.propagation.TextMapPropagator
import zio.ZLayer
import zio.telemetry.opentelemetry.tracing.Tracing

object TarotTracingLayer {
  val tarotTracingLive: ZLayer[Tracing, Throwable, TarotTracing] =
    ZLayer.fromFunction { (tracing: Tracing) =>
      TarotTracingLive(tracing)
    }
}
