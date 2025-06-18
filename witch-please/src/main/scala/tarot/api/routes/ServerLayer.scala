package tarot.api.routes

import tarot.api.middlewares.{LoggingMiddleware, TracingMiddleware}
import tarot.layers.AppEnv
import zio.{ZIO, ZLayer}
import zio.http.{Response, Routes, Server}

object ServerLayer {
  val serverLive: ZLayer[AppEnv & Routes[AppEnv, Response], Throwable, Server] =
    ZLayer.scoped {
      for {
        routes <- ZIO.service[Routes[AppEnv, Response]]
        middlewares = TracingMiddleware.tracing ++ LoggingMiddleware.logging
        httpApp = routes @@ middlewares
        server <- Server.serve(httpApp).provideSomeLayer[AppEnv & Routes[AppEnv, Response]](
          Server.defaultWithPort(8080)
        )
      } yield server
    }
}
