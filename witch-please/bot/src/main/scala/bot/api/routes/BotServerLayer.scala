package bot.api.routes

import bot.layers.BotEnv
import zio.{ZIO, ZLayer}
import zio.http.{Response, Routes, Server}

object BotServerLayer {
  val serverLive: ZLayer[BotEnv & Routes[BotEnv, Response], Throwable, Server] =
    ZLayer.scoped {
      for {
        routes <- ZIO.service[Routes[BotEnv, Response]]
        //middlewares = TracingMiddleware.tracing ++ LoggingMiddleware.logging
        httpApp = routes //@@ middlewares
        server <- Server.serve(httpApp).provideSomeLayer[BotEnv & Routes[BotEnv, Response]](
          Server.defaultWithPort(8080)
        )
      } yield server
    }
}
