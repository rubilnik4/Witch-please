package tarot.api.routes

import shared.infrastructure.telemetry.middleware.*
import tarot.layers.TarotEnv
import zio.{ZIO, ZLayer}
import zio.http.{Response, Routes, Server}
import zio.telemetry.opentelemetry.tracing.Tracing

import java.net.InetSocketAddress

object TarotServerLayer {
  private val tracingLayer: ZLayer[TarotEnv, Nothing, Tracing] =
    ZLayer.fromFunction((env: TarotEnv) => env.telemetryTracing.tracing)
    
  val serverLive: ZLayer[TarotEnv & Routes[TarotEnv, Response], Throwable, Server] =
    ZLayer.scoped {
      for {
        config <- ZIO.serviceWith[TarotEnv](_.config)
        routes <- ZIO.service[Routes[TarotEnv, Response]]
        middlewares = TracingMiddleware.tracing ++ LoggingMiddleware.logging
        httpApp = routes @@ middlewares
        server <- Server.serve(httpApp)
          .provideSomeLayer[TarotEnv & Routes[TarotEnv, Response]](
            Server.defaultWith(_.binding(new InetSocketAddress("0.0.0.0", config.project.port))) ++
              tracingLayer
          )
      } yield server
    }
}
