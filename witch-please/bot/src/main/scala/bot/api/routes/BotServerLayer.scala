package bot.api.routes

import bot.layers.BotEnv
import shared.infrastructure.telemetry.middleware.*
import zio.http.{Response, Routes, Server}
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.{ZIO, ZLayer}

object BotServerLayer {
  private val tracingLayer: ZLayer[BotEnv, Nothing, Tracing] =
    ZLayer.fromFunction((env: BotEnv) => env.telemetryTracing.tracing)

  val serverLive: ZLayer[BotEnv & Routes[BotEnv, Response], Throwable, Server] =
    ZLayer.scoped {
      for {
        config <- ZIO.serviceWith[BotEnv](_.config)
        routes <- ZIO.service[Routes[BotEnv, Response]]
        middlewares = TracingMiddleware.tracing ++ LoggingMiddleware.logging
        httpApp = routes @@ middlewares
        server <- Server.serve(httpApp)
          .provideSomeLayer[BotEnv & Routes[BotEnv, Response]](
            Server.defaultWithPort(config.project.port) ++ tracingLayer
          )
      } yield server
    }
}
