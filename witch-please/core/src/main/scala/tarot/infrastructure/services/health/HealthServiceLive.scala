package tarot.infrastructure.services.health

import tarot.infrastructure.repositories.health.HealthDao
import zio.ZIO

final class HealthServiceLive(healthDao: HealthDao) extends HealthService {
  override def ready: ZIO[Any, Throwable, Unit] =
    healthDao.ready
}
