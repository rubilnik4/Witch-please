package tarot.api.routes

import sttp.apispec.openapi.{Info, OpenAPI}
import sttp.tapir.ztapir.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import tarot.api.endpoints.*
import tarot.layers.TarotEnv
import zio.{ZIO, ZLayer}
import zio.http.*

object TarotRoutesLayer {
  private val endpoints: List[ZServerEndpoint[TarotEnv, Any]] =
    UserEndpoint.endpoints ++ AuthorEndpoint.endpoints ++  AuthEndpoint.endpoints ++
      SpreadEndpoint.endpoints ++ CardEndpoint.endpoints

  private val openApiDocs =
    SwaggerInterpreter().fromEndpoints[[T] =>> ZIO[TarotEnv, Throwable, T]](
      endpoints.map(_.endpoint),
      Info("Tarot API", "1.0")
    )

  val apiRoutesLive: ZLayer[TarotEnv, Throwable, Routes[TarotEnv, Response]] =
    ZLayer.fromFunction { (env: TarotEnv) =>
      ZioHttpInterpreter().toHttp(endpoints ++ openApiDocs )
    }
}
