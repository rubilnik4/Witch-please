package shared.infrastructure.telemetry.middleware

import io.opentelemetry.api.trace.SpanKind
import zio.ZIO
import zio.http.{Handler, Middleware, Request, Routes}
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.telemetry.opentelemetry.tracing.propagation.TraceContextPropagator

object TracingMiddleware {
  def tracing: Middleware[Tracing] =
    new Middleware[Tracing] {
      override def apply[Env1 <: Tracing, Err](routes: Routes[Env1, Err]): Routes[Env1, Err] =
        routes.transform { handler =>
          Handler.scoped[Env1] {
            Handler.fromFunctionZIO[Request] { req =>
              for {
                tracing <- ZIO.service[Tracing]

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