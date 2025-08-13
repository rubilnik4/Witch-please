package shared.infrastructure.services.common

import zio.{Clock, UIO}

import java.time.Instant

object DateTimeService {
  def getDateTimeNow: UIO[Instant] = 
    Clock.instant
}