package tarot.application.telemetry.tracing

import zio.telemetry.opentelemetry.tracing.Tracing

final case class TarotTracingLive(tracing: Tracing) 
  extends TarotTracing 