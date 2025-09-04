package tarot.api.routes

import tarot.api.infrastructure.{LoggingMiddleware, TracingMiddleware}
import tarot.layers.TarotEnv
import zio.{ZIO, ZLayer}
import zio.http.{Response, Routes, Server}

object TarotServerLayer {
  val serverLive: ZLayer[TarotEnv & Routes[TarotEnv, Response], Throwable, Server] =
    ZLayer.scoped {
      for {
        routes <- ZIO.service[Routes[TarotEnv, Response]]
        middlewares = TracingMiddleware.tracing ++ LoggingMiddleware.logging
        httpApp = routes @@ middlewares
        server <- Server.serve(httpApp).provideSomeLayer[TarotEnv & Routes[TarotEnv, Response]](
          Server.defaultWithPort(8080)
        )
      } yield server
    }
}
