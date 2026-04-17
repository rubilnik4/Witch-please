package tarot.infrastructure.services.health

import tarot.infrastructure.repositories.health.HealthDao
import zio.ZLayer

object HealthServiceLayer {
  val live: ZLayer[HealthDao, Nothing, HealthService] =
    ZLayer.fromFunction(HealthServiceLive.apply)
}
