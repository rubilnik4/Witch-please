package bot.api.endpoints

import bot.layers.BotEnv
import sttp.model.StatusCode
import sttp.tapir.ztapir.*
import zio.ZIO

object HealthEndpoint {
  private val healthEndpoint: ZServerEndpoint[BotEnv, Any] =
    endpoint.get
      .in("health")
      .out(statusCode(StatusCode.Ok))
      .zServerLogic(_ => ZIO.unit)

  private val liveEndpoint: ZServerEndpoint[BotEnv, Any] =
    endpoint.get
      .in("health" / "live")
      .out(statusCode(StatusCode.Ok))
      .zServerLogic(_ => ZIO.unit)

  private val readyEndpoint: ZServerEndpoint[BotEnv, Any] =
    endpoint.get
      .in("health" / "ready")
      .out(statusCode(StatusCode.Ok))
      .zServerLogic(_ => ZIO.unit)

  val endpoints: List[ZServerEndpoint[BotEnv, Any]] =
    List(healthEndpoint, liveEndpoint, readyEndpoint)
}
