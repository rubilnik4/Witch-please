package tarot.api.middlewares

import tarot.layers.AppEnv
import zio.LogLevel
import zio.http.{HandlerAspect, Header, Middleware, Routes, Status}

object LoggingMiddleware {
  def logging: Middleware[AppEnv] = {
    new Middleware[AppEnv] {
      def apply[Env1 <: AppEnv, Err](routes: Routes[Env1, Err]): Routes[Env1, Err] =
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
