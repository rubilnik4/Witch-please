package shared.infrastructure.services.common

import zio.{Clock, UIO}

import java.time.*

object DateTimeService {
  def getDateTimeNow: UIO[Instant] = 
    Clock.instant

  def currentLocalDate(zone: ZoneId): UIO[LocalDate] =
    getDateTimeNow.map(i => ZonedDateTime.ofInstant(i, zone).toLocalDate)

  def currentYearMonth(zone: ZoneId): UIO[YearMonth] =
    getDateTimeNow.map(i => YearMonth.from(ZonedDateTime.ofInstant(i, zone)))
}