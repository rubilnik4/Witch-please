package tarot.application.telemetry.tracing

import zio.telemetry.opentelemetry.tracing.Tracing

trait TarotTracing {
  def tracing: Tracing
}
