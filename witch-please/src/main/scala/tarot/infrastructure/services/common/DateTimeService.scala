package tarot.infrastructure.services.common

import java.time.Instant

object DateTimeService {
  def getDateTimeNow: Instant = Instant.now()
}