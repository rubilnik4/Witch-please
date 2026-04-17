package bot.api.routes

import bot.api.endpoints.{HealthEndpoint, WebhookEndpoint}
import bot.layers.BotEnv
import sttp.apispec.openapi.{Info, OpenAPI}
import sttp.tapir.ztapir.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import zio.{ZIO, ZLayer}
import zio.http.*

object BotRoutesLayer {
  private val endpoints: List[ZServerEndpoint[BotEnv, Any]] =
    HealthEndpoint.endpoints ++ WebhookEndpoint.endpoints

  val apiRoutesLive: ZLayer[BotEnv, Throwable, Routes[BotEnv, Response]] =
    ZLayer.fromFunction { (env: BotEnv) =>
      ZioHttpInterpreter().toHttp(endpoints)
    }
}
