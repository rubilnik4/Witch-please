package bot.api.routes

import bot.api.endpoints.WebhookEndpoint
import bot.layers.AppEnv
import sttp.apispec.openapi.{Info, OpenAPI}
import sttp.tapir.ztapir.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import zio.{ZIO, ZLayer}
import zio.http.*

object BotRoutesLayer {
  private val endpoints: List[ZServerEndpoint[AppEnv, Any]] =
    WebhookEndpoint.endpoints

  val apiRoutesLive: ZLayer[AppEnv, Throwable, Routes[AppEnv, Response]] =
    ZLayer.fromFunction { (env: AppEnv) =>
      ZioHttpInterpreter().toHttp(endpoints)
    }
}
