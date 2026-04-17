package tarot.infrastructure.services.health

import zio.ZIO

trait HealthService {
  def ready: ZIO[Any, Throwable, Unit]
}
