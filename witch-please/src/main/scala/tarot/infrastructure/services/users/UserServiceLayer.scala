package tarot.infrastructure.services.users

import tarot.application.configurations.AppConfig
import zio.{Task, ZLayer}

object UserServiceLayer {
  val userServiceLive: ZLayer[AppConfig, Nothing, UserService] =
    ZLayer.succeed(UserServiceLive())
}
