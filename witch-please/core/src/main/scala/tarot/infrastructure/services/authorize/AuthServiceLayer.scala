package tarot.infrastructure.services.authorize

import tarot.application.configurations.TarotConfig
import tarot.infrastructure.repositories.TarotRepositoryLayer
import tarot.infrastructure.repositories.users.{UserProjectRepository, UserRepository}
import zio.{Task, ZLayer}

object AuthServiceLayer {
  val live: ZLayer[UserRepository & UserProjectRepository, Throwable, AuthService] =    
    ZLayer.fromFunction(AuthServiceLive(_, _))
}
