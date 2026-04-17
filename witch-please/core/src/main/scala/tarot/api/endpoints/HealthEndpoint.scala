package tarot.api.endpoints

import sttp.model.StatusCode
import sttp.tapir.ztapir.*
import tarot.layers.TarotEnv
import zio.ZIO

object HealthEndpoint {
  private val healthEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.get
      .in("health")
      .out(statusCode(StatusCode.Ok))
      .zServerLogic(_ => ZIO.unit)

  private val liveEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.get
      .in("health" / "live")
      .out(statusCode(StatusCode.Ok))
      .zServerLogic(_ => ZIO.unit)

  private val readyEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.get
      .in("health" / "ready")
      .out(statusCode(StatusCode.Ok))
      .errorOut(statusCode(StatusCode.ServiceUnavailable))
      .zServerLogic(_ =>
        ZIO.serviceWithZIO[TarotEnv](_.services.healthService.ready)
          .orElseFail(StatusCode.ServiceUnavailable)
      )

  val endpoints: List[ZServerEndpoint[TarotEnv, Any]] =
    List(healthEndpoint, liveEndpoint, readyEndpoint)
}
