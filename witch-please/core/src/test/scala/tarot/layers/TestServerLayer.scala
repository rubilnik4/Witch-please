package tarot.layers

import tarot.api.routes.RoutesLayer
import tarot.models.{TestProjectState, TestSpreadState}
import zio.{Ref, Scope, ULayer, ZLayer}
import zio.http.{Server, TestServer}

object TestServerLayer {
  val testServerLayer: ZLayer[AppEnv & Scope & TestServer, Throwable, Unit] =
    ZLayer.fromZIO (
      for {
        routes <- RoutesLayer.apiRoutesLive.build.map(_.get)
        _ <- TestServer.addRoutes(routes)
      } yield ()
    )

  val serverConfig: ULayer[Server.Config] = ZLayer.succeed(
    Server.Config.default.port(8080)
  )
}
