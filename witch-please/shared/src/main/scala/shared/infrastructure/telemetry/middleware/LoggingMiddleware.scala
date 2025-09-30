package shared.infrastructure.telemetry.middleware

import zio.LogLevel
import zio.http.*

object LoggingMiddleware {
  def logging: Middleware[Any] = {
    new Middleware[Any] {
      def apply[Env1, Err](routes: Routes[Env1, Err]): Routes[Env1, Err] =
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
