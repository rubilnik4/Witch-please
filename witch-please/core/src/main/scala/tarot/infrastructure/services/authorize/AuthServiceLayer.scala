package tarot.infrastructure.services.authorize

import tarot.application.configurations.TarotConfig
import zio.{Task, ZLayer}

object AuthServiceLayer {
  val authServiceLive: ZLayer[TarotConfig, Nothing, AuthService] =
    ZLayer.succeed(AuthServiceLive())
}
