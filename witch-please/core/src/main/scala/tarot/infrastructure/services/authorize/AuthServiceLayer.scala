package tarot.infrastructure.services.authorize

import tarot.infrastructure.repositories.users.*
import zio.ZLayer

object AuthServiceLayer {
  val live: ZLayer[UserRepository & UserProjectRepository, Nothing, AuthService] =    
    ZLayer.fromFunction(AuthServiceLive(_, _))
}
