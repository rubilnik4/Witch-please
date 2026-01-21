package tarot.api.routes

import sttp.apispec.openapi.Info
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.*
import tarot.api.endpoints.*
import tarot.layers.TarotEnv
import zio.http.*
import zio.{ZIO, ZLayer}

object TarotRoutesLayer {
  val endpoints: List[ZServerEndpoint[TarotEnv, Any]] =
    UserEndpoint.endpoints ++ ChannelEndpoint.endpoints ++ AuthorEndpoint.endpoints ++  AuthEndpoint.endpoints ++
    SpreadEndpoint.endpoints ++ CardEndpoint.endpoints ++ CardOfDayEndpoint.endpoints

  private val openApiDocs =
    SwaggerInterpreter().fromEndpoints[[T] =>> ZIO[TarotEnv, Throwable, T]](
      endpoints.map(_.endpoint),
      Info("Tarot API", "1.0")
    )

  val live: ZLayer[TarotEnv, Throwable, Routes[TarotEnv, Response]] =
    ZLayer.fromFunction { (env: TarotEnv) =>
      ZioHttpInterpreter().toHttp(endpoints ++ openApiDocs)
    }
}
