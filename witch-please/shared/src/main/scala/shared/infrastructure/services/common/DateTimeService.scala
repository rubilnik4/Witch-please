package shared.infrastructure.services.common

import zio.{Clock, UIO}

import java.time.*

object DateTimeService {
  private final val Zone = ZoneId.of("Europe/Moscow")
  
  def getDateTimeNow: UIO[Instant] = 
    Clock.instant
    
  def currentLocalDate(zone: ZoneId = Zone): UIO[LocalDate] =
    getDateTimeNow.map(i => ZonedDateTime.ofInstant(i, zone).toLocalDate)

  def currentYearMonth(zone: ZoneId = Zone): UIO[YearMonth] =
    getDateTimeNow.map(i => YearMonth.from(ZonedDateTime.ofInstant(i, zone)))
}