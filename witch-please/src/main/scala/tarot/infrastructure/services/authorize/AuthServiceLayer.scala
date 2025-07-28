package tarot.infrastructure.services.authorize

import tarot.application.configurations.AppConfig
import zio.{Task, ZLayer}

object AuthServiceLayer {
  val authServiceLive: ZLayer[AppConfig, Nothing, AuthService] =
    ZLayer.succeed(AuthServiceLive())
}
