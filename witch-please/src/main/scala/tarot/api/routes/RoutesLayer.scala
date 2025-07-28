package tarot.api.routes

import sttp.apispec.openapi.{Info, OpenAPI}
import sttp.tapir.ztapir.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import tarot.api.endpoints.*
import tarot.layers.AppEnv
import zio.{ZIO, ZLayer}
import zio.http.*

object RoutesLayer {
  private val endpoints: List[ZServerEndpoint[AppEnv, Any]] =
    UserEndpoint.endpoints ++ AuthEndpoint.endpoints ++
      ProjectEndpoint.endpoints ++ SpreadEndpoint.endpoints

  private val openApiDocs =
    SwaggerInterpreter().fromEndpoints[[T] =>> ZIO[AppEnv, Throwable, T]](
      endpoints.map(_.endpoint),
      Info("Tarot API", "1.0")
    )

  val apiRoutesLive: ZLayer[AppEnv, Throwable, Routes[AppEnv, Response]] =
    ZLayer.fromFunction { (env: AppEnv) =>
      ZioHttpInterpreter().toHttp(endpoints ++ openApiDocs )
    }
}
