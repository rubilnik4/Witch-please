package tarot.api.infrastructure

import io.opentelemetry.api.trace.SpanKind
import tarot.layers.TarotEnv
import zio.ZIO
import zio.http.{Handler, Middleware, Request, Routes}

import zio.telemetry.opentelemetry.tracing.propagation.TraceContextPropagator

object TracingMiddleware {
  def tracing: Middleware[TarotEnv] =
    new Middleware[TarotEnv] {
      override def apply[Env1 <: TarotEnv, Err](routes: Routes[Env1, Err]): Routes[Env1, Err] =
        routes.transform { handler =>
          Handler.scoped[Env1] {
            Handler.fromFunctionZIO[Request] { req =>
              for {
                tracing <- ZIO.serviceWith[TarotEnv](_.telemetryTracing.tracing)

                headersMap = req.headers.toList
                  .map(h => h.headerName -> h.renderedValue)
                  .toMap
                
                response <- tracing.extractSpan(
                  propagator = TraceContextPropagator.default,
                  carrier = SimpleIncomingCarrier(headersMap),
                  spanName = s"${req.method.toString} ${req.url.path.toString}",
                  spanKind = SpanKind.SERVER
                ) {
                  handler.runZIO(req)
                }
              } yield response
            }
          }
        }
    }
}