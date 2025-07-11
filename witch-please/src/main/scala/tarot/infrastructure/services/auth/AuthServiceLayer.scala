package tarot.infrastructure.services.auth

import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import tarot.application.configurations.AppConfig
import zio.{Task, ZLayer}

object AuthServiceLayer {
  val authServiceLive: ZLayer[AppConfig, Nothing, AuthService] =
    ZLayer.succeed(AuthServiceLive())
}
