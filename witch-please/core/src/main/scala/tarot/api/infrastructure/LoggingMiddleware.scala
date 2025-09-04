package tarot.api.infrastructure

import tarot.layers.TarotEnv
import zio.LogLevel
import zio.http.{HandlerAspect, Header, Middleware, Routes, Status}

object LoggingMiddleware {
  def logging: Middleware[TarotEnv] = {
    new Middleware[TarotEnv] {
      def apply[Env1 <: TarotEnv, Err](routes: Routes[Env1, Err]): Routes[Env1, Err] =
        Routes.fromIterable(
          routes.routes.map(route => route.transform[Env1](_ @@ HandlerAspect.requestLogging(
            level = _ => LogLevel.Debug,
            logRequestBody = true,
            logResponseBody = true,
            loggedRequestHeaders = Set(Header.ContentType),
            loggedResponseHeaders = Set(Header.ContentType)
          )))
        )
    }
  }
}
